package com.sigmundgranaas.forgero.data.pipeline.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.common.tags.engine.TagLoadingService;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.data.generation.api.ComponentGenerator;
import com.sigmundgranaas.forgero.data.generation.api.GeneratedState;
import com.sigmundgranaas.forgero.data.generation.impl.ComponentGeneratorImpl;
import com.sigmundgranaas.forgero.data.loading.impl.codec.*;
import com.sigmundgranaas.forgero.data.mapper.api.ComponentMapper;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyBuilder;
import com.sigmundgranaas.forgero.data.mapper.impl.AttributePropertyBuilder;
import com.sigmundgranaas.forgero.data.mapper.impl.FeaturePropertyBuilder;
import com.sigmundgranaas.forgero.data.processing.api.DataProcessor;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import com.sigmundgranaas.forgero.data.processing.api.RawDefinition;
import com.sigmundgranaas.forgero.data.processing.impl.DataProcessorImpl;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Orchestrates the entire Forgero data loading and component generation pipeline.
 * This class should be called once during application initialization.
 */
public class ForgeroDataInitializer {

	private final TagGraph tagGraph;
	private final TaggedRegistry<Component> componentRegistry;

	public ForgeroDataInitializer(String defaultNamespace) {
		IdentifierFactory identifierFactory = new IdentifierFactory.Builder().defaultNamespace(defaultNamespace).build();

		// --- Prerequisite: Tag Loading ---
		TagLoadingService tagLoadingService = new TagLoadingService(identifierFactory);
		this.tagGraph = tagLoadingService.loadTags(new OpenIdentifier(defaultNamespace, "tags"));

		// --- Stage 1: Raw Data Loading ---
		Map<OpenIdentifier, RawDefinition> rawDefinitions = loadRawDefinitions(identifierFactory);

		// --- Stage 2: Dependency Resolution & Normalization ---
		DataProcessor dataProcessor = new DataProcessorImpl();
		NormalizedState normalizedState = dataProcessor.normalize(rawDefinitions);

		// --- Stage 3: Combinatorial Generation ---
		ComponentGenerator componentGenerator = new ComponentGeneratorImpl(identifierFactory);
		GeneratedState generatedState = componentGenerator.generate(normalizedState, this.tagGraph);

		// --- Stage 4: Component Mapping ---
		ComponentMapper componentMapper = new ComponentMapper(identifierFactory);
		List<Component> components = new ArrayList<>();

		// Map basic components
		normalizedState.materials().values().forEach(dto -> components.add(componentMapper.map(dto)));
		normalizedState.shapes().values().forEach(dto -> components.add(componentMapper.map(dto)));
		normalizedState.staticParts().values().forEach(dto -> components.add(componentMapper.map(dto)));
		normalizedState.schematics().values().forEach(dto -> components.add(componentMapper.map(dto))); // Schematics are self-contained

		// Map generated parts (these depend on materials and shapes, which should now be in cache)
		generatedState.parts().values().forEach(dto -> components.add(componentMapper.map(dto, normalizedState)));

		// Map generated equipment (these depend on static parts and generated parts, which should now be in cache)
		generatedState.equipment().values().forEach(dto -> components.add(componentMapper.map(dto)));


		// --- Stage 5: Registration ---
		TaggedRegistry.Builder<Component> registryBuilder = new TaggedRegistry.Builder<>(this.tagGraph);
		components.forEach(registryBuilder::add);
		this.componentRegistry = registryBuilder.build();
	}

	private Map<OpenIdentifier, RawDefinition> loadRawDefinitions(IdentifierFactory idFactory) {
		ResourceConverter<RawDefinition> converter = (stream, id) -> {
			try {
				String jsonContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
				JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();
				OpenIdentifier type = idFactory.of(root.get("type").getAsString());

				Object rawDto = switch (type.path()) {
					case "material" ->
							MaterialCodecs.MATERIAL_DATA_CODEC.parse(JsonOps.INSTANCE, root).getOrThrow(false, msg -> {
							});
					case "shape" -> ShapeCodecs.SHAPE_DATA_CODEC.parse(JsonOps.INSTANCE, root).getOrThrow(false, msg -> {
					});
					case "part_template" ->
							PartTemplateCodecs.PART_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, root).getOrThrow(false, msg -> {
							});
					case "equipment_template", "tool_template" ->
							EquipmentTemplateCodecs.TOOL_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, root).getOrThrow(false, msg -> {
							});
					case "schematic" ->
							SchematicCodecs.SCHEMATIC_DATA_CODEC.parse(JsonOps.INSTANCE, root).getOrThrow(false, msg -> {
							});
					case "static_part" ->
							StaticPartCodecs.STATIC_PART_DATA_CODEC.parse(JsonOps.INSTANCE, root).getOrThrow(false, msg -> {
							});
					default -> null;
				};

				if (rawDto == null) return Optional.empty();
				OpenIdentifier canonicalId = idFactory.of(id.namespace(), id.name());
				return Optional.of(new RawDefinition(canonicalId, rawDto));
			} catch (IOException e) {
				return Optional.empty();
			} catch (Exception e) {
				return Optional.empty();
			}
		};

		ResourceProvider dataProvider = new ClassPathResourceProvider("data");
		ResourceLoader<RawDefinition> dataLoader = new ResourceLoader<>(dataProvider, converter);
		String ns = "forgero";

		return Stream.of("materials", "shapes", "parts", "equipment", "schematics")
				.flatMap(dir -> dataLoader.load(new OpenIdentifier(ns, dir), true))
				.collect(Collectors.toMap(RawDefinition::id, Function.identity(), (existing, replacement) -> existing));
	}

	public TaggedRegistry<Component> getComponentRegistry() {
		return componentRegistry;
	}

	public TagGraph getTagGraph() {
		return tagGraph;
	}
}
