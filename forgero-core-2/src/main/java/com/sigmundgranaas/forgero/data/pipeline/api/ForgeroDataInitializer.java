package com.sigmundgranaas.forgero.data.pipeline.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.common.tags.engine.TagLoadingService;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;
import com.sigmundgranaas.forgero.core.property.predicate.TagMatchCondition;
import com.sigmundgranaas.forgero.data.generation.api.ComponentGenerator;
import com.sigmundgranaas.forgero.data.generation.api.GeneratedState;
import com.sigmundgranaas.forgero.data.generation.impl.ComponentGeneratorImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.MaterialData;
import com.sigmundgranaas.forgero.data.loading.api.data.SchematicData;
import com.sigmundgranaas.forgero.data.loading.api.data.ShapeData;
import com.sigmundgranaas.forgero.data.loading.api.data.StaticPartData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.*;
import com.sigmundgranaas.forgero.data.mapper.api.ComponentMapper;
import com.sigmundgranaas.forgero.data.processing.api.DataProcessor;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import com.sigmundgranaas.forgero.data.processing.api.RawDefinition;
import com.sigmundgranaas.forgero.data.processing.impl.DataProcessorImpl;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Orchestrates the entire Forgero data loading and component generation pipeline.
 * This class should be called once during application initialization.
 */
public class ForgeroDataInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(ForgeroDataInitializer.class);

	private final ForgeroDataBundle dataBundle;
	private final Codec<MaterialData> materialCodec;
	private final Codec<ShapeData> shapeCodec;
	private final Codec<PartTemplateData> partTemplateCodec;
	private final Codec<EquipmentTemplateData> equipmentTemplateCodec;
	private final Codec<SchematicData> schematicCodec;
	private final Codec<StaticPartData> staticPartCodec;
	private final IdentifierFactory identifierFactory;

	public ForgeroDataInitializer(String defaultNamespace) {
		LOGGER.info("Starting Forgero data initialization pipeline...");
		long pipelineStartTime = System.currentTimeMillis();

		// =================================
		// 1. SETUP
		// =================================
		this.identifierFactory = new IdentifierFactory.Builder().defaultNamespace(defaultNamespace).build();
		var conditionCodec = setupConditionCodec();
		Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		FeatureCodecs.registerCodecs(conditionCodec);
		Codec<List<FeatureData>> featureListCodec = FeatureCodecs.createFeatureDataListCodec();
		Codec<List<UpgradeSlotData>> upgradeSlotDataListCodec = Codec.list(PartTemplateCodecs.UPGRADE_SLOT_DATA_CODEC);

		this.materialCodec = MaterialCodecs.create(attributeListCodec, featureListCodec);
		this.shapeCodec = ShapeCodecs.create(attributeListCodec, featureListCodec);
		this.partTemplateCodec = PartTemplateCodecs.create(attributeListCodec, featureListCodec, upgradeSlotDataListCodec);
		this.equipmentTemplateCodec = EquipmentTemplateCodecs.create(attributeListCodec, featureListCodec, upgradeSlotDataListCodec);
		this.schematicCodec = SchematicCodecs.create();
		this.staticPartCodec = StaticPartCodecs.create(attributeListCodec, featureListCodec, upgradeSlotDataListCodec);

		// =================================
		// 2. TAG LOADING
		// =================================
		TagLoadingService tagLoadingService = new TagLoadingService(identifierFactory);
		TagGraph tagGraph = tagLoadingService.loadTags(new OpenIdentifier(defaultNamespace, "tags"));

		// =================================
		// 3. RAW DEFINITION LOADING
		// =================================
		Map<OpenIdentifier, RawDefinition> forgeroDefs = loadRawDefinitions(defaultNamespace);
		Map<OpenIdentifier, RawDefinition> mcDefs = loadRawDefinitions("minecraft");
		var combined = new HashMap<OpenIdentifier, RawDefinition>();
		combined.putAll(forgeroDefs);
		combined.putAll(mcDefs);

		// =================================
		// 4. NORMALIZATION
		// =================================
		DataProcessor dataProcessor = new DataProcessorImpl();
		NormalizedState normalizedState = dataProcessor.normalize(combined);
		LOGGER.info("Normalized raw definitions into: {} materials, {} shapes, {} schematics, {} static parts, {} part templates, {} equipment templates.",
				normalizedState.materials().size(),
				normalizedState.shapes().size(),
				normalizedState.schematics().size(),
				normalizedState.staticParts().size(),
				normalizedState.partTemplates().size(),
				normalizedState.equipmentTemplates().size());

		// =================================
		// 5. COMPONENT GENERATION
		// =================================
		ComponentGenerator componentGenerator = new ComponentGeneratorImpl(identifierFactory);
		GeneratedState generatedState = componentGenerator.generate(normalizedState, tagGraph);
		LOGGER.info("Generated {} parts and {} equipment from templates.", generatedState.parts().size(), generatedState.equipment().size());

		// =================================
		// 6. HOST DATA COLLECTION
		// =================================
		Map<OpenIdentifier, HostData> hostItemMap = collectHostData(normalizedState, generatedState);
		LOGGER.info("Collected {} host item data mappings.", hostItemMap.size());

		// =================================
		// 7. COMPONENT MAPPING
		// =================================
		ComponentMapper componentMapper = new ComponentMapper(identifierFactory);
		List<Component> components = new ArrayList<>();
		mapAllComponents(normalizedState, generatedState, componentMapper, components);
		LOGGER.info("Mapped all definitions to {} pristine components.", components.size());

		// =================================
		// 8. FINAL REGISTRY & BUNDLE CREATION
		// =================================
		TaggedRegistry.Builder<Component> registryBuilder = new TaggedRegistry.Builder<>(tagGraph);
		components.forEach(registryBuilder::add);
		TaggedRegistry<Component> componentRegistry = registryBuilder.build();

		this.dataBundle = new ForgeroDataBundle(componentRegistry, tagGraph, hostItemMap);

		long pipelineEndTime = System.currentTimeMillis();
		LOGGER.info("Forgero data initialization complete. Took {}ms.", pipelineEndTime - pipelineStartTime);
		LOGGER.info("Final Bundle: {} components, {} host mappings.", dataBundle.componentRegistry().all().size(), dataBundle.hostItemMap().size());
	}

	private Codec<Condition> setupConditionCodec() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		staticCodecs.put("forgero:self_has_tag", TagMatchCondition.CODEC);
		staticCodecs.put("forgero:root_has_tag", TagMatchCondition.CODEC);
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		return new ConditionCodec(staticCodecs, dynamicCodecs);
	}

	private Map<OpenIdentifier, RawDefinition> loadRawDefinitions(String defaultNamespace) {
		long startTime = System.currentTimeMillis();
		LOGGER.info("Loading raw definitions from resources...");

		ResourceConverter<RawDefinition> converter = this::parseDefinition;
		ResourceProvider dataProvider = new ClassPathResourceProvider("data");
		ResourceLoader<RawDefinition> dataLoader = new ResourceLoader<>(dataProvider, converter);

		Map<OpenIdentifier, RawDefinition> definitions = Stream.of("materials", "shapes", "parts", "equipment", "schematics")
				.flatMap(dir -> dataLoader.load(new OpenIdentifier(defaultNamespace, dir), true))
				.collect(Collectors.toMap(RawDefinition::id, Function.identity(), (existing, replacement) -> {
					LOGGER.warn("Duplicate definition ID found: [{}]. The existing entry will be kept.", existing.id());
					return existing;
				}));

		long endTime = System.currentTimeMillis();
		LOGGER.info("Loaded {} raw definitions in {}ms.", definitions.size(), endTime - startTime);
		return definitions;
	}

	private Optional<RawDefinition> parseDefinition(InputStream stream, OpenIdentifier id) {
		try {
			String jsonContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
			JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();

			if (!root.has("type") || !root.get("type").isJsonPrimitive()) {
				LOGGER.error("Parsing failed for [{}]: Missing or invalid 'type' field. It must be a string.", id);
				return Optional.empty();
			}
			OpenIdentifier type = identifierFactory.of(root.get("type").getAsString());
			Object rawDto = switch (type.path()) {
				case "material" -> materialCodec.parse(JsonOps.INSTANCE, root)
						.getOrThrow(false, errorMsg -> logCodecError(id, "material", errorMsg));
				case "shape" -> shapeCodec.parse(JsonOps.INSTANCE, root)
						.getOrThrow(false, errorMsg -> logCodecError(id, "shape", errorMsg));
				case "part_template" -> partTemplateCodec.parse(JsonOps.INSTANCE, root)
						.getOrThrow(false, errorMsg -> logCodecError(id, "part_template", errorMsg));
				case "equipment_template", "tool_template" -> equipmentTemplateCodec.parse(JsonOps.INSTANCE, root)
						.getOrThrow(false, errorMsg -> logCodecError(id, "equipment_template", errorMsg));
				case "schematic" -> schematicCodec.parse(JsonOps.INSTANCE, root)
						.getOrThrow(false, errorMsg -> logCodecError(id, "schematic", errorMsg));
				case "static_part" -> staticPartCodec.parse(JsonOps.INSTANCE, root)
						.getOrThrow(false, errorMsg -> logCodecError(id, "static_part", errorMsg));
				default -> {
					LOGGER.warn("Unsupported definition type '{}' in file [{}]. Skipping.", type, id);
					yield null;
				}
			};

			if (rawDto == null) return Optional.empty();
			OpenIdentifier canonicalId = identifierFactory.of(id.namespace(), id.name());
			return Optional.of(new RawDefinition(canonicalId, rawDto));
		} catch (IOException e) {
			LOGGER.error("Failed to read resource file [{}]: {}", id, e.getMessage());
		} catch (JsonSyntaxException e) {
			LOGGER.error("Invalid JSON syntax in file [{}]: {}", id, e.getMessage());
		} catch (IllegalStateException e) {
			LOGGER.error("Terminating parsing for [{}] due to a critical codec error. Check previous log message for details.", id);
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while parsing file [{}]: {}", id, e.getMessage(), e);
		}
		return Optional.empty();
	}

	private void logCodecError(OpenIdentifier fileId, String codecType, String errorMessage) {
		LOGGER.error("Codec parsing failed for {} [{}]. Details: {}", codecType, fileId, errorMessage);
	}

	private void mapAllComponents(NormalizedState normalizedState, GeneratedState generatedState, ComponentMapper componentMapper, List<Component> components) {
		normalizedState.materials().values().forEach(dto -> components.add(componentMapper.map(dto)));
		normalizedState.shapes().values().forEach(dto -> components.add(componentMapper.map(dto)));
		normalizedState.staticParts().values().forEach(dto -> components.add(componentMapper.map(dto)));
		normalizedState.schematics().values().forEach(dto -> components.add(componentMapper.map(dto)));
		generatedState.parts().values().forEach(dto -> components.add(componentMapper.map(dto, normalizedState)));
		generatedState.equipment().values().forEach(dto -> components.add(componentMapper.map(dto)));
	}

	private Map<OpenIdentifier, HostData> collectHostData(NormalizedState normalizedState, GeneratedState generatedState) {
		Map<OpenIdentifier, HostData> hostItemMap = new HashMap<>();
		normalizedState.materials().values().stream().filter(dto -> dto.host() != null).forEach(dto -> hostItemMap.put(dto.id(), dto.host()));
		normalizedState.shapes().values().stream().filter(dto -> dto.host() != null).forEach(dto -> hostItemMap.put(dto.id(), dto.host()));
		normalizedState.staticParts().values().stream().filter(dto -> dto.host() != null).forEach(dto -> hostItemMap.put(dto.id(), dto.host()));
		normalizedState.schematics().values().stream().filter(dto -> dto.host() != null).forEach(dto -> hostItemMap.put(dto.id(), dto.host()));
		generatedState.parts().values().stream().filter(dto -> dto.host() != null).forEach(dto -> hostItemMap.put(dto.id(), dto.host()));
		generatedState.equipment().values().stream().filter(dto -> dto.host() != null).forEach(dto -> hostItemMap.put(dto.id(), dto.host()));
		return Map.copyOf(hostItemMap);
	}

	public ForgeroDataBundle getDataBundle() {
		return dataBundle;
	}
}
