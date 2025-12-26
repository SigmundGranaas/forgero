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

	public TagLoadingService(IdentifierFactory factory) {
		this.factory = factory;
		// The service is configured to look in the /data directory of the classpath.
		ResourceProvider provider = new ClassPathResourceProvider("data");

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

				// Create the canonical OpenIdentifier for the tag.
				// The factory.of() method will now handle the final normalization to a single-group name.
				// So, "materials/material" becomes "material" here.
				OpenIdentifier tagId = factory.of(id.namespace(), tagRelativePath);

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
			// When resolving parent IDs from strings (like "forgero:materials/material" from JSON),
			// the factory.of(String) method is used. This method will automatically
			// canonicalize these parent IDs (e.g., "forgero:materials/material" -> "forgero:material").
			// This ensures all IDs in the TagGraph are canonical.
			Set<OpenIdentifier> parentIds = def.parents().stream()
					.map(factory::of)
					.collect(Collectors.toSet());
			builder.add(def.id(), parentIds);
		});

		return builder.build();
	}

	/**
	 * Internal DTO to hold a parsed tag definition along with its identifier.
	 */
	private record IdentifiableTagDefinition(OpenIdentifier id, Set<String> parents) implements Identifiable {
	}
}
