package com.sigmundgranaas.forgero.data.v3.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.tags.engine.TagLoadingService;
import com.sigmundgranaas.forgero.core.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.data.generator.ComponentGenerator; // New import
import com.sigmundgranaas.forgero.data.generator.ComponentGeneratorImpl; // New import
import com.sigmundgranaas.forgero.data.generator.ComponentMapper;
import com.sigmundgranaas.forgero.data.processor.DataProcessor; // New import
import com.sigmundgranaas.forgero.data.processor.DataProcessorImpl;
import com.sigmundgranaas.forgero.data.v3.codec.*; // Import all codecs
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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

	private final IdentifierFactory identifierFactory;
	private final TagGraph tagGraph;
	private final TaggedRegistry<Component> componentRegistry; // The final, queryable registry of Components

	public ForgeroDataInitializer(String defaultNamespace) {
		this.identifierFactory = new IdentifierFactory.Builder().defaultNamespace(defaultNamespace).build();

		// --- Stage 0: Tag Loading (Prerequisite for TaggedRegistry) ---
		// Delegate tag loading entirely to TagLoadingService, which handles stripping prefixes.
		TagLoadingService tagLoadingService = new TagLoadingService(this.identifierFactory);
		// Load tags from the "tags" subdirectory within the "forgero" namespace.
		// The root path passed to load will be a path-like OpenIdentifier (e.g., "forgero:tags").
		this.tagGraph = tagLoadingService.loadTags(new OpenIdentifier("forgero", "tags"));


		// --- Stage 1: Raw Resource Loading (collect all raw DTOs, wrapped in IdentifiedTopLevelData) ---
		Map<OpenIdentifier, TopLevelData> rawIdentifiedData = loadRawComponentData(identifierFactory);


		// --- Stage 2: Dependency Resolution & Normalization ---
		DataProcessor dataProcessor = new DataProcessorImpl();
		// The DataProcessor processes IdentifiedTopLevelData, resolving 'include' dependencies
		Map<OpenIdentifier, TopLevelData> normalizedData = dataProcessor.process(rawIdentifiedData);


		// --- Stage 3: Combinatorial Generation ---
		ComponentGenerator componentGenerator = new ComponentGeneratorImpl(this.identifierFactory);
		// The generator takes the normalized data and the TagGraph to produce generated DTOs.
		// It outputs IdentifiedTopLevelData objects (wrapping PartTemplateData or ToolTemplateData with concrete structures)
		Map<OpenIdentifier, TopLevelData> generatedComponentsData = componentGenerator.generate(normalizedData, this.tagGraph);

		// Combine normalized original data with newly generated data for the mapping stage.
		// IMPORTANT: Only MaterialData, StaticPartData, and SchematicData are kept from original `normalizedData`
		// as these represent concrete components. Templates are blueprints and are NOT mapped directly.
		Map<OpenIdentifier, TopLevelData> allComponentsDataForMapping = normalizedData.values().stream()
				.filter(data -> {
					Object unwrapped = data.unwrapAs(Object.class);
					return unwrapped instanceof MaterialData ||
							unwrapped instanceof StaticPartData ||
							unwrapped instanceof SchematicData;
				})
				.collect(Collectors.toMap(TopLevelData::id, Function.identity()));

		// Add all generated parts and tools. These have unique IDs (e.g. iron-pickaxe_head) and will not clash.
		allComponentsDataForMapping.putAll(generatedComponentsData);

		// Debugging output to see what's being mapped
		System.out.println("Components selected for mapping:");
		allComponentsDataForMapping.forEach((id, data) -> System.out.println("- " + id.toString() + " (" + data.unwrapAs(Object.class).getClass().getSimpleName() + ")"));


		// --- Stage 4: Component Mapping (DTOs to actual Forgero Component objects) ---
		// The ComponentMapper needs all processed DTOs to resolve internal references recursively.
		ComponentMapper componentMapper = new ComponentMapper(this.identifierFactory, allComponentsDataForMapping);

		// Map all DTOs into concrete Component objects
		Map<OpenIdentifier, Component> finalMappedComponents = new HashMap<>();
		for (TopLevelData data : allComponentsDataForMapping.values()) {
			componentMapper.map(data.id()).ifPresent(component -> finalMappedComponents.put(component.id(), component));
		}

		// --- Stage 5: Registration (into the TaggedRegistry) ---
		// Components are added to the TaggedRegistry, which uses the TagGraph for validation.
		TaggedRegistry.Builder<Component> registryBuilder = new TaggedRegistry.Builder<>(this.tagGraph);
		for (Component component : finalMappedComponents.values()) {
			// The TaggedRegistry.Builder will validate that all tags on the Component exist in the TagGraph.
			// If IdentifiedTopLevelData (which ComponentGenerator uses) has correctly merged tags,
			// and ComponentMapper passes them, this step ensures consistency.
			registryBuilder.add(component);
		}
		this.componentRegistry = registryBuilder.build();

		// At this point, `componentRegistry` is ready to be used by the rest of the application.
	}

	/**
	 * Helper method to load raw component data from resources.
	 * This method dynamically determines the DTO type based on the JSON content
	 * and loads only from component-specific subdirectories, excluding tags.
	 */
	private Map<OpenIdentifier, TopLevelData> loadRawComponentData(IdentifierFactory idFactory) {
		ResourceConverter<TopLevelData> componentDataConverter = (stream, id) -> {
			try {
				String jsonContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
				JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();

				// Read the raw 'type' string from JSON and let the factory canonicalize it.
				// This handles cases like "forgero:material" or "material".
				OpenIdentifier type = idFactory.of(root.get("type").getAsString());

				Object rawDto;
				// Use the correct codec based on the type
				if (type.equals(idFactory.of("forgero:material"))) {
					rawDto = MaterialCodecs.MATERIAL_DATA_CODEC.parse(JsonOps.INSTANCE, root)
							.getOrThrow(false, msg -> System.err.println("Error parsing material " + id + ": " + msg));
				} else if (type.equals(idFactory.of("forgero:part_template"))) {
					rawDto = PartTemplateCodecs.PART_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, root)
							.getOrThrow(false, msg -> System.err.println("Error parsing part template " + id + ": " + msg));
				} else if (type.equals(idFactory.of("forgero:tool_template"))) {
					rawDto = ToolTemplateCodecs.TOOL_TEMPLATE_DATA_CODEC.parse(JsonOps.INSTANCE, root)
							.getOrThrow(false, msg -> System.err.println("Error parsing tool template " + id + ": " + msg));
				} else if (type.equals(idFactory.of("forgero:schematic"))) {
					rawDto = SchematicCodecs.SCHEMATIC_DATA_CODEC.parse(JsonOps.INSTANCE, root)
							.getOrThrow(false, msg -> System.err.println("Error parsing schematic " + id + ": " + msg));
				} else if (type.equals(idFactory.of("forgero:static_part"))) {
					// This handles parsing of the StaticPartData DTO.
					rawDto = StaticPartCodecs.STATIC_PART_DATA_CODEC.parse(JsonOps.INSTANCE, root)
							.getOrThrow(false, msg -> System.err.println("Error parsing static part " + id + ": " + msg));
				} else {
					// If a file is in a component directory but has an unknown type, log and skip.
					System.err.println("Unknown top-group data type for " + id + ": " + type);
					return Optional.empty();
				}

				// The 'id' parameter here is a resource path identifier (e.g., "forgero:materials/iron.json").
				// We want the resulting IdentifiedTopLevelData to have a canonical ID (e.g., "forgero:iron").
				// OpenIdentifier.name() correctly extracts the filename without extension (e.g., "iron").
				// Then, idFactory.of() will take this "iron" and ensure it's fully canonical in the new OpenIdentifier.
				return Optional.of(new IdentifiedTopLevelData(idFactory.of(id.namespace(), id.name()), rawDto));
			} catch (IOException e) {
				System.err.println("Failed to read resource stream for " + id + ": " + e.getMessage());
				return Optional.empty();
			} catch (Exception e) { // Catch any other exceptions during parsing (e.g., NullPointerException if "type" is missing)
				System.err.println("Error parsing resource " + id + " (likely missing 'type' field or malformed): " + e.getMessage());
				return Optional.empty(); // Treat as skipped if it can't be parsed correctly
			}
		};

		ResourceProvider dataProvider = new ClassPathResourceProvider("data");
		ResourceLoader<TopLevelData> dataLoader = new ResourceLoader<>(dataProvider, componentDataConverter);

		// Load data explicitly from known component directories, *excluding* the 'tags' directory.
		// The `new OpenIdentifier("forgero", "materials")` here creates an OpenIdentifier
		// whose path is "materials". This is passed to the ResourceLoader which
		// correctly expects a path-like OpenIdentifier for discovery.
		Stream<TopLevelData> materials = dataLoader.load(new OpenIdentifier("forgero", "materials"), true);
		Stream<TopLevelData> parts = dataLoader.load(new OpenIdentifier("forgero", "parts"), true);
		Stream<TopLevelData> tools = dataLoader.load(new OpenIdentifier("forgero", "tools"), true);
		Stream<TopLevelData> schematics = dataLoader.load(new OpenIdentifier("forgero", "schematics"), true);
		// If you have a dedicated 'static_parts' folder, uncomment the line below.
		// Based on your provided resources, static_oak_handle.json is in 'parts/', so it's already covered by the 'parts' stream.
		// Stream<TopLevelData> staticParts = dataLoader.load(idFactory.of("forgero", "static_parts"), true);


		// Concatenate all streams and collect into a single Map.
		// `flatMap(Function.identity())` is used to flatten the Stream of Streams.
		return Stream.of(materials, parts, tools, schematics /*, staticParts */)
				.flatMap(Function.identity())
				.collect(Collectors.toMap(TopLevelData::id, Function.identity(), (existing, replacement) -> {
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
