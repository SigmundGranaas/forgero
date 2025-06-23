package com.sigmundgranaas.forgero.tag.tagloading;

import com.sigmundgranaas.forgero.core.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.core.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.core.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.core.resource.loader.implementation.ClassPathResourceProvider;
import com.sigmundgranaas.forgero.core.resource.loader.implementation.ResourceLoader;
import com.sigmundgranaas.forgero.core.tags.engine.TagDefinition;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraphBuilder;
import com.sigmundgranaas.forgero.core.tags.engine.TagParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A high-level service for loading a TagGraph from a resource directory.
 */
public class TagLoadingService {

	private final IdentifierFactory factory;
	private final ResourceLoader<IdentifiableTagDefinition> resourceLoader;
	private final TagParser tagParser = new TagParser();
	private static final String JSON_EXTENSION = ".json";

	public TagLoadingService(IdentifierFactory factory) {
		this.factory = factory;
		// The service is configured to look in the /data directory of the classpath.
		ResourceProvider provider = new ClassPathResourceProvider("data");

		// The converter reads the stream, parses it into a TagDefinition, and pairs it with its cleaned ID.
		ResourceConverter<IdentifiableTagDefinition> converter = (stream, id) -> {
			try {
				String path = id.path();
				if (!path.endsWith(JSON_EXTENSION)) {
					// We only care about .json files for tag definitions.
					return Optional.empty();
				}

				// The actual tag identifier is the file path WITHOUT the .json extension.
				String tagPath = path.substring(0, path.length() - JSON_EXTENSION.length());
				OpenIdentifier tagId = new OpenIdentifier(id.namespace(), tagPath);

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
	 * Loads all tag files from a given root path and constructs a TagGraph.
	 *
	 * @param rootPath The root identifier path to load tags from (e.g., "forgero:tags").
	 * @return A fully constructed and validated TagGraph.
	 */
	public TagGraph loadTags(OpenIdentifier rootPath) {
		TagGraphBuilder builder = new TagGraphBuilder();
		Stream<IdentifiableTagDefinition> definitions = resourceLoader.load(rootPath, true);

		definitions.forEach(def -> {
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
