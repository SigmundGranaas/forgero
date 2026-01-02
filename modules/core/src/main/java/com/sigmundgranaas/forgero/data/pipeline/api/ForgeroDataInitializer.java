package com.sigmundgranaas.forgero.data.pipeline.api;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.cof.ComponentConstructor;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.DefinitionCodecRegistry;
import com.sigmundgranaas.forgero.data.loading.impl.codec.PartTemplateCodecs;
import com.sigmundgranaas.forgero.data.pipeline.impl.*;
import com.sigmundgranaas.forgero.data.pipeline.util.IdTemplateResolver;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ForgeroDataInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(ForgeroDataInitializer.class);

	private final ForgeroDataBundle dataBundle;

	/**
	 * Configuration record for the data initializer, providing all necessary dependencies.
	 *
	 * @param defaultNamespace      The default namespace for identifiers.
	 * @param resourceProvider      The provider for loading raw data files.
	 * @param tagResolver           The pre-loaded and merged TagResolver.
	 * @param propertyCodecs        A map of all property codecs to be used for parsing.
	 * @param staticConditionCodecs A map of codecs for custom static conditions.
	 * @param dynamicConditionCodecs A map of codecs for custom dynamic conditions.
	 */
	public record Config(
			String defaultNamespace,
			ResourceProvider resourceProvider,
			TagResolver tagResolver,
			Map<PropertyKey<?>, Codec<? extends List<?>>> propertyCodecs,
			Map<String, Codec<? extends StaticCondition>> staticConditionCodecs,
			Map<String, Codec<? extends DynamicCondition>> dynamicConditionCodecs
	) {
	}


	public ForgeroDataInitializer(Config config) {
		long startTime = System.currentTimeMillis();
		LOGGER.info("Starting Forgero data initialization pipeline...");

		// 1. SETUP: Factories, Codecs, and Resolvers
		IdentifierFactory identifierFactory = new IdentifierFactory.Builder().defaultNamespace(config.defaultNamespace()).build();
		// Use the pre-loaded TagResolver from the config
		TagResolver tagResolver = config.tagResolver();
		Codec<Condition> conditionCodec = new com.sigmundgranaas.forgero.core.condition.api.ConditionCodec(config.staticConditionCodecs(), config.dynamicConditionCodecs());
		Codec<List<UpgradeSlotData>> upgradeSlotDataListCodec = Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC);

		// Use batch-enabled codec registry to support both traditional and batch attribute formats
		DefinitionCodecRegistry codecRegistry = DefinitionCodecRegistry.createWithBatchSupport(conditionCodec, upgradeSlotDataListCodec);
		ResourceConverter<RawDefinition> converter = new RawDefinitionConverter(identifierFactory, codecRegistry);
		ResourceLoader<RawDefinition> dataLoader = new ResourceLoader<>(config.resourceProvider(), converter);

		// Prepare for multi-namespace loading
		List<String> namespaces = List.of(config.defaultNamespace(), "minecraft");
		List<String> definitionDirs = List.of("materials", "shapes", "parts", "equipment", "schematics", "casts");

		// 2. LOAD RAW DEFINITIONS from all configured namespaces
		Map<OpenIdentifier, RawDefinition> rawDefinitions = namespaces.stream()
				.flatMap(ns -> definitionDirs.stream().map(dir -> new OpenIdentifier(ns, dir)))
				.flatMap(path -> dataLoader.load(path, true))
				.collect(Collectors.toMap(RawDefinition::id, Function.identity(), (existing, replacement) -> {
					LOGGER.warn("Duplicate definition ID found: [{}]. The existing entry will be kept.", existing.id());
					return existing;
				}));

		// 2b. LOAD EXTENSIONS from all configured namespaces
		Map<OpenIdentifier, RawDefinition> extensionDefinitions = namespaces.stream()
				.map(ns -> new OpenIdentifier(ns, "extensions"))
				.flatMap(path -> dataLoader.load(path, true))
				.collect(Collectors.toMap(RawDefinition::id, Function.identity(), (existing, replacement) -> {
					LOGGER.warn("Duplicate extension ID found: [{}]. The existing entry will be kept.", existing.id());
					return existing;
				}));

		// 3. MERGE EXTENSIONS into their targets
		if (!extensionDefinitions.isEmpty()) {
			Map<OpenIdentifier, RawDefinition> combined = new HashMap<>(rawDefinitions);
			combined.putAll(extensionDefinitions);
			ExtensionMerger extensionMerger = new ExtensionMerger();
			rawDefinitions = extensionMerger.merge(combined);
		}

		// 4. INSTANTIATE SERVICES
		IncludeResolver includeResolver = new IncludeResolver(rawDefinitions);
		PropertyMerger propertyMerger = new PropertyMerger(config.propertyCodecs());
		CofComponentConverter cofConverter = new CofComponentConverter(identifierFactory);

		// 5. PROCESS STATIC DEFINITIONS (non-templates)
		Map<OpenIdentifier, CofComponent> staticComponents = new HashMap<>();
		Map<OpenIdentifier, HostData> hostItemMap = new HashMap<>();

		List<RawDefinition> staticDefinitions = rawDefinitions.values().stream()
				.filter(def -> !(def.data() instanceof PartTemplateData) && !(def.data() instanceof EquipmentTemplateData))
				.toList();

		for (RawDefinition def : staticDefinitions) {
			List<DefinitionData> chain = includeResolver.resolve(def.id());
			PropertyMerger.MergedResult merged = propertyMerger.merge(chain);
			CofComponent cof = cofConverter.convert(def, merged);
			staticComponents.put(cof.id(), cof);
			if (merged.host() != null) {
				hostItemMap.put(cof.id(), merged.host());
			} else if (isSchematic(def)) {
				// Auto-generate host data for schematics so they can be registered as items
				HostData autoHost = createSchematicHostData(cof.id());
				hostItemMap.put(cof.id(), autoHost);
			}
		}
		LOGGER.info("Processed {} static definitions.", staticComponents.size());

		// 6. PROCESS TEMPLATES
		IdTemplateResolver idTemplateResolver = new IdTemplateResolver(identifierFactory, rawDefinitions);
		TemplateGenerator templateGenerator = new TemplateGenerator(identifierFactory, tagResolver, propertyMerger, idTemplateResolver, staticComponents, rawDefinitions);
		TemplateGenerator.TemplateResult templateResult = templateGenerator.generate();

		Map<OpenIdentifier, CofComponent> allCofComponents = new HashMap<>(staticComponents);
		templateResult.components().forEach(comp -> allCofComponents.put(comp.id(), comp));

		hostItemMap.putAll(templateResult.hostData());
		LOGGER.info("Generated {} components from templates.", templateResult.components().size());

		// 7. BUILD FINAL RUNTIME COMPONENTS
		ComponentConstructor componentConstructorRegistry = ComponentConstructor.defaults();
		ComponentBuilder componentBuilder = new ComponentBuilder(componentConstructorRegistry, allCofComponents);
		List<Component> components = componentBuilder.buildAll();
		LOGGER.info("Built {} final runtime components.", components.size());

		// 8. CREATE FINAL BUNDLE
		TaggedRegistry.Builder<Component> registryBuilder = new TaggedRegistry.Builder<>(tagResolver);
		components.forEach(registryBuilder::add);
		this.dataBundle = new ForgeroDataBundle(registryBuilder.build(), tagResolver, Collections.unmodifiableMap(hostItemMap));

		long endTime = System.currentTimeMillis();
		LOGGER.info("Forgero data initialization complete. Total time: {}ms", endTime - startTime);
	}

	public ForgeroDataBundle getDataBundle() {
		return dataBundle;
	}

	/**
	 * Checks if the definition represents a schematic type.
	 */
	private boolean isSchematic(RawDefinition def) {
		OpenIdentifier type = def.data().type();
		return type != null && type.path().equalsIgnoreCase("schematic");
	}

	/**
	 * Creates default host data for a schematic so it can be registered as an item.
	 * Schematics use the part_item class and go in the ingredients item group.
	 */
	private HostData createSchematicHostData(OpenIdentifier componentId) {
		CreateData createData = new CreateData(
				componentId,
				"forgero:part_item",
				"minecraft:ingredients"
		);
		return new HostData(null, createData);
	}
}
