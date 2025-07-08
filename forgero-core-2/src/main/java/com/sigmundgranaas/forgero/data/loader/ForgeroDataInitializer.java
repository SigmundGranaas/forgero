package com.sigmundgranaas.forgero.data.loader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.data.definition.GeneratedState;
import com.sigmundgranaas.forgero.core.data.definition.NormalizedState;
import com.sigmundgranaas.forgero.core.data.definition.RawDefinition;
import com.sigmundgranaas.forgero.core.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.tags.engine.TagLoadingService;
import com.sigmundgranaas.forgero.core.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.data.generator.ComponentGenerator;
import com.sigmundgranaas.forgero.data.generator.ComponentGeneratorImpl;
import com.sigmundgranaas.forgero.data.mapper.ComponentMapper;
import com.sigmundgranaas.forgero.data.processor.DataProcessor;
import com.sigmundgranaas.forgero.data.processor.DataProcessorImpl;
import com.sigmundgranaas.forgero.data.v3.codec.*;
import com.sigmundgranaas.forgero.data.v3.dto.*;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
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

		// Map basic components (Materials, Shapes, StaticParts, Schematics) first, as they are dependencies for generated components
		normalizedState.materials().values().forEach(material -> components.add(componentMapper.map(material)));
		normalizedState.shapes().values().forEach(shape -> components.add(componentMapper.map(shape)));
		normalizedState.staticParts().values().forEach(staticPart -> components.add(componentMapper.map(staticPart)));
		normalizedState.schematics().values().forEach(schematic -> components.add(componentMapper.map(schematic, normalizedState))); // Schematics are self-contained

		// Map generated parts (these depend on materials and shapes, which should now be in cache)
		generatedState.parts().values().forEach(generatedPart -> components.add(componentMapper.map(generatedPart, normalizedState)));

		// Map generated equipment (these depend on static parts and generated parts, which should now be in cache)
		generatedState.equipment().values().forEach(generatedEquipment -> components.add(componentMapper.map(generatedEquipment, generatedState)));


		// --- Stage 5: Registration ---
		TaggedRegistry.Builder<Component> registryBuilder = new TaggedRegistry.Builder<>(this.tagGraph);
		components.forEach(registryBuilder::add);
		this.componentRegistry = registryBuilder.build();
	}

	/**
	 * Helper method to load raw component data from resources.
	 * This method dynamically determines the DTO type based on the JSON content
	 * and loads only from component-specific subdirectories, excluding tags.
	 */
	private Map<OpenIdentifier, RawDefinition> loadRawDefinitions(IdentifierFactory idFactory) {
		ResourceConverter<RawDefinition> converter = (stream, id) -> {
			try {
				String jsonContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
				JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();

				// Read the raw 'type' string from JSON and let the factory canonicalize it.
				OpenIdentifier type = idFactory.of(root.get("type").getAsString());

				Object rawDto;
				// Use the correct codec based on the type's path
				rawDto = switch (type.path()) {
					case "material" -> MaterialCodecs.MATERIAL_DATA_CODEC.parse(JsonOps.INSTANCE, root)
							.getOrThrow(false, msg -> System.err.println("Error parsing material " + id + ": " + msg));
					case "shape" -> ShapeCodecs.SHAPE_DATA_CODEC.parse(JsonOps.INSTANCE, root)
							.getOrThrow(false, msg -> System.err.println("Error parsing shape " + id + ": " + msg));
					case "part_template" -> PartTemplateCodecs.PART_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, root)
							.getOrThrow(false, msg -> System.err.println("Error parsing part template " + id + ": " + msg));
					case "tool_template" -> EquipmentTemplateCodecs.TOOL_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, root)
							.getOrThrow(false, msg -> System.err.println("Error parsing tool template " + id + ": " + msg));
					case "schematic" -> SchematicCodecs.SCHEMATIC_DATA_CODEC.parse(JsonOps.INSTANCE, root)
							.getOrThrow(false, msg -> System.err.println("Error parsing schematic " + id + ": " + msg));
					case "static_part" -> StaticPartCodecs.STATIC_PART_DATA_CODEC.parse(JsonOps.INSTANCE, root)
							.getOrThrow(false, msg -> System.err.println("Error parsing static part " + id + ": " + msg));
					default -> {
						System.err.println("Unknown top-group data type for " + id + ": " + type);
						yield null;
					}
				};

				if (rawDto == null) return Optional.empty();

				// The 'id' parameter here is a resource path identifier (e.g., "forgero:materials/iron.json").
				// We want the resulting RawDefinition to have a canonical ID (e.g., "forgero:iron").
				OpenIdentifier canonicalId = idFactory.of(id.namespace(), id.name());
				return Optional.of(new RawDefinition(canonicalId, rawDto));
			} catch (IOException e) {
				System.err.println("Failed to read resource stream for " + id + ": " + e.getMessage());
				return Optional.empty();
			} catch (Exception e) {
				System.err.println("Error parsing resource " + id + " (likely missing 'type' field or malformed JSON): " + e.getMessage());
				return Optional.empty();
			}
		};

		ResourceProvider dataProvider = new ClassPathResourceProvider("data");
		ResourceLoader<RawDefinition> dataLoader = new ResourceLoader<>(dataProvider, converter);
		String ns = "forgero";

		// Load data explicitly from known component directories, *excluding* the 'tags' directory.
		return Stream.of("materials", "shapes", "parts", "tools", "schematics") // NEW: Added "shapes"
				.flatMap(dir -> dataLoader.load(new OpenIdentifier(ns, dir), true))
				.collect(Collectors.toMap(RawDefinition::id, Function.identity(), (existing, replacement) -> {
					System.err.println("Duplicate data ID found: " + existing.id() + ". Using existing.");
					return existing;
				}));
	}

	/**
	 * Returns the fully initialized TaggedRegistry of Components.
	 * This is the entry point for other parts of the application to query game items.
	 * @return The TaggedRegistry of Components.
	 */
	public TaggedRegistry<Component> getComponentRegistry() {
		return componentRegistry;
	}

	/**
	 * Returns the loaded TagGraph.
	 * @return The TagGraph.
	 */
	public TagGraph getTagGraph() {
		return tagGraph;
	}
}
