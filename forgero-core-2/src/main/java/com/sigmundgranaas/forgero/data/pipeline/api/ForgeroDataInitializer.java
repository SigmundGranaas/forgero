package com.sigmundgranaas.forgero.data.pipeline.api;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.cof.ComponentConstructor;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.common.tags.engine.TagLoadingService;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.*;
import com.sigmundgranaas.forgero.data.pipeline.impl.*;
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
import java.util.stream.Stream;

public class ForgeroDataInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(ForgeroDataInitializer.class);

	private final ForgeroDataBundle dataBundle;

	/**
	 * Configuration record for the data initializer, providing all necessary dependencies.
	 *
	 * @param defaultNamespace      The default namespace for identifiers.
	 * @param resourceProvider      The provider for loading raw data files.
	 * @param propertyCodecs        A map of all property codecs to be used for parsing.
	 * @param staticConditionCodecs A map of codecs for custom static conditions.
	 * @param dynamicConditionCodecs A map of codecs for custom dynamic conditions.
	 */
	public record Config(
			String defaultNamespace,
			ResourceProvider resourceProvider,
			Map<String, Codec<? extends List<?>>> propertyCodecs,
			Map<String, Codec<? extends StaticCondition>> staticConditionCodecs,
			Map<String, Codec<? extends DynamicCondition>> dynamicConditionCodecs
	) {
	}


	public ForgeroDataInitializer(Config config) {
		long startTime = System.currentTimeMillis();
		LOGGER.info("Starting Forgero data initialization pipeline...");

		// 1. SETUP: Factories, Codecs, and Graphs
		IdentifierFactory identifierFactory = new IdentifierFactory.Builder().defaultNamespace(config.defaultNamespace()).build();
		Codec<Condition> conditionCodec = new ConditionCodec(config.staticConditionCodecs(), config.dynamicConditionCodecs());
		Codec<List<AttributeData>> attributeDataListCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		Codec<List<UpgradeSlotData>> upgradeSlotDataListCodec = Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC);

		var rawDtoCodecs = new RawDtoCodecs(RawDtoCodecs.defaultMap(attributeDataListCodec, upgradeSlotDataListCodec));
		ResourceConverter<RawDefinition> converter = new RawDefinitionConverter(identifierFactory, rawDtoCodecs::codecFor);
		ResourceLoader<RawDefinition> dataLoader = new ResourceLoader<>(config.resourceProvider(), converter);


		// 2. LOAD RAW DEFINITIONS
		Map<OpenIdentifier, RawDefinition> rawDefinitions = Stream.of("materials", "shapes", "parts", "equipment", "schematics")
				.flatMap(dir -> dataLoader.load(new OpenIdentifier(identifierFactory.defaultNamespace(), dir), true))
				.collect(Collectors.toMap(RawDefinition::id, Function.identity(), (existing, replacement) -> {
					LOGGER.warn("Duplicate definition ID found: [{}]. The existing entry will be kept.", existing.id());
					return existing;
				}));


		// 3. LOAD TAGS
		TagGraph tagGraph = new TagLoadingService(identifierFactory).loadTags(new OpenIdentifier(config.defaultNamespace(), "tags"));


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
			List<Object> chain = includeResolver.resolve(def.id());
			PropertyMerger.MergedResult merged = propertyMerger.merge(chain);
			CofComponent cof = cofConverter.convert(def, merged);
			staticComponents.put(cof.id(), cof);
			if (merged.host() != null) {
				hostItemMap.put(cof.id(), merged.host());
			}
		}
		LOGGER.info("Processed {} static definitions.", staticComponents.size());

		// 6. PROCESS TEMPLATES
		TemplateGenerator templateGenerator = new TemplateGenerator(identifierFactory, tagGraph, propertyMerger, staticComponents, rawDefinitions);
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
		TaggedRegistry.Builder<Component> registryBuilder = new TaggedRegistry.Builder<>(tagGraph);
		components.forEach(registryBuilder::add);
		this.dataBundle = new ForgeroDataBundle(registryBuilder.build(), tagGraph, Collections.unmodifiableMap(hostItemMap));

		long endTime = System.currentTimeMillis();
		LOGGER.info("Forgero data initialization complete. Total time: {}ms", endTime - startTime);
	}

	public ForgeroDataBundle getDataBundle() {
		return dataBundle;
	}

	private static class RawDtoCodecs {
		private final Map<String, Codec<?>> codecMap;

		private RawDtoCodecs(Map<String, Codec<?>> codecMap) {
			this.codecMap = codecMap;
		}

		public static Map<String, Codec<?>> defaultMap(Codec<List<AttributeData>> attributeCodec, Codec<List<UpgradeSlotData>> upgradeSlotCodec) {
			Map<String, Codec<?>> codecMap = new HashMap<>();
			codecMap.put("material", MaterialCodecs.create(attributeCodec));
			codecMap.put("shape", ShapeCodecs.create(attributeCodec));
			codecMap.put("schematic", SchematicCodecs.create());
			codecMap.put("static_part", StaticPartCodecs.create(attributeCodec, upgradeSlotCodec));
			codecMap.put("part_template", PartTemplateCodecs.create(attributeCodec, upgradeSlotCodec));
			codecMap.put("equipment_template", EquipmentTemplateCodecs.create(attributeCodec, upgradeSlotCodec));
			codecMap.put("tool_template", EquipmentTemplateCodecs.create(attributeCodec, upgradeSlotCodec)); // Legacy support

			return codecMap;
		}

		public Codec<?> codecFor(String type) {
			return codecMap.get(type);
		}
	}
}
