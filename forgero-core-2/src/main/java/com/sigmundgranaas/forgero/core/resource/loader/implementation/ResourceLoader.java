package com.sigmundgranaas.forgero.core.resource.loader.implementation;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.core.resource.loader.api.ResourceProvider;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A generic loader for discovering and parsing resources from a given ResourceProvider.
 *
 * @param <T> The type of resource this loader is configured to produce.
 */
public class ResourceLoader<T> {
	private final ResourceProvider provider;
	private final ResourceConverter<T> converter;

	public ResourceLoader(ResourceProvider provider, ResourceConverter<T> converter) {
		this.provider = provider;
		this.converter = converter;
	}

	/**
	 * Loads all resources found within a given root path.
	 * It lists all files using the ResourceProvider, then attempts to read and convert each one.
	 *
	 * @param rootPath  The root identifier to start the resource discovery from.
	 * @param recursive If true, loading will include all subdirectories.
	 * @return A Stream of successfully loaded and converted resources.
	 */
	public Stream<T> load(OpenIdentifier rootPath, boolean recursive) {
		return provider.list(rootPath, recursive)
				.parallel() // Reading and parsing can be done in parallel for efficiency
				.map(this::tryLoadResource)
				.flatMap(Optional::stream); // Flattens the stream of Optionals, filtering out empty ones
	}

	private Optional<T> tryLoadResource(OpenIdentifier id) {
		Optional<InputStream> streamOpt = provider.read(id);
		if (streamOpt.isEmpty()) {
			// Log this event? For now, we just skip it.
			return Optional.empty();
		}

		try (InputStream stream = streamOpt.get()) {
			return converter.convert(stream, id);
		} catch (Exception e) {
			// It's crucial to catch exceptions during conversion to prevent one bad file from failing the entire stream.
			// Ideally, log the exception with the resource ID.
			// e.g., System.err.println("Failed to load resource: " + id + " - " + e.getMessage());
			return Optional.empty();
		}
	}
}
