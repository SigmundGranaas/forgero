// File: /home/sigmund/Documents/projects/forgero/1-20/forgero-core-2/src/main/java/com/sigmundgranaas/forgero/core/tags/engine/TagLoadingService.java
package com.sigmundgranaas.forgero.common.tags.engine;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A high-group service for loading a TagGraph from a resource directory.
 */
public class TagLoadingService {

	private final IdentifierFactory factory;
	private final ResourceLoader<IdentifiableTagDefinition> resourceLoader;
	private final TagParser tagParser = new TagParser();
	private static final String JSON_EXTENSION = ".json";
	private static final String TAGS_PATH_FOLDER = "tags/"; // The specific folder name for tags

	/**
	 * Creates a TagLoadingService with a default ClassPathResourceProvider.
	 * Use {@link #TagLoadingService(IdentifierFactory, ResourceProvider)} for custom resource providers.
	 */
	public TagLoadingService(IdentifierFactory factory) {
		this(factory, new ClassPathResourceProvider("data"));
	}

	/**
	 * Creates a TagLoadingService with a custom ResourceProvider.
	 * This allows platform-specific implementations (e.g., Fabric mod container-based loading).
	 *
	 * @param factory  The identifier factory for creating identifiers.
	 * @param provider The resource provider to use for loading tag files.
	 */
	public TagLoadingService(IdentifierFactory factory, ResourceProvider provider) {
		this.factory = factory;

		// The converter reads the stream, parses it into a TagDefinition, and pairs it with its cleaned ID.
		ResourceConverter<IdentifiableTagDefinition> converter = (stream, id) -> {
			try {
				String rawPath = id.path(); // Example: "tags/materials/iron.json"

				// Ensure it's a JSON file
				if (!rawPath.endsWith(JSON_EXTENSION)) {
					return Optional.empty();
				}

				// Check if the resource is in the designated 'tags/' folder.
				// This acts as a filter to ensure only actual tag definitions are processed by this service.
				if (!rawPath.startsWith(TAGS_PATH_FOLDER)) {
					return Optional.empty();
				}

				// Extract the part of the path that represents the tag's actual name/location in the graph.
				// E.g., "tags/materials/material.json" -> "materials/material"
				String tagRelativePath = rawPath.substring(TAGS_PATH_FOLDER.length());
				if (tagRelativePath.endsWith(JSON_EXTENSION)) {
					tagRelativePath = tagRelativePath.substring(0, tagRelativePath.length() - JSON_EXTENSION.length());
				}

				OpenIdentifier tagId = new OpenIdentifier(id.namespace(), tagRelativePath);

				String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
				TagDefinition definition = tagParser.parse(content);
				return Optional.of(new IdentifiableTagDefinition(tagId, definition.parents()));
			} catch (IOException e) {
				return Optional.empty();
			}
		};

		this.resourceLoader = new ResourceLoader<>(provider, converter);
	}

	/**
	 * Loads all tag files from a given root path and constructs a TagResolver.
	 *
	 * @param rootPath The root identifier path to load tags from (e.g., "forgero:tags").
	 * @return A fully constructed and validated TagResolver.
	 */
	public TagResolver loadTags(OpenIdentifier rootPath) {
		TagGraphBuilder builder = new TagGraphBuilder();
		// The rootPath passed to load is still a path-like OpenIdentifier (e.g., "forgero:tags").
		Stream<IdentifiableTagDefinition> definitions = resourceLoader.load(rootPath, true);

		definitions.forEach(def -> {
			Set<OpenIdentifier> parentIds = def.parents().stream()
					.map(TagLoadingService::parseTagIdentifier)
					.collect(Collectors.toSet());
			builder.add(def.id(), parentIds);
		});

		return builder.build();
	}

	private static OpenIdentifier parseTagIdentifier(String id) {
		if (id.contains(":")) {
			return OpenIdentifier.parse(id);
		}
		return new OpenIdentifier("forgero", id);
	}

	private record IdentifiableTagDefinition(OpenIdentifier id, Set<String> parents) implements Identifiable {
	}
}
