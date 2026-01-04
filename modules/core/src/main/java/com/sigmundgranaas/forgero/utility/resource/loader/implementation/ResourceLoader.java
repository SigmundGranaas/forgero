package com.sigmundgranaas.forgero.utility.resource.loader.implementation;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A generic loader for discovering and parsing resources from a given ResourceProvider.
 * <p>
 * ResourceLoader combines a {@link ResourceProvider} (for discovery and reading) with a
 * {@link ResourceConverter} (for parsing) and a {@link ResourceFilter} (for filtering).
 * <p>
 * Resources are loaded in parallel for efficiency, with per-resource error handling
 * to prevent one bad file from failing the entire load operation.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Using builder pattern (recommended)
 * ResourceLoader<Material> loader = ResourceLoader.<Material>builder()
 *     .provider(compositeProvider)
 *     .converter(new JsonCodecConverter<>(Material.CODEC))
 *     .filter(ResourceFilter.JSON)
 *     .build();
 *
 * Stream<Material> materials = loader.load(
 *     ResourcePath.directory("forgero", "materials"),
 *     true
 * );
 *
 * // Legacy usage (still supported)
 * ResourceLoader<Material> legacyLoader = new ResourceLoader<>(provider, converter);
 * Stream<Material> data = legacyLoader.load(new OpenIdentifier("forgero", "materials"), true);
 * }</pre>
 *
 * @param <T> The type of resource this loader is configured to produce.
 */
public class ResourceLoader<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoader.class);

	private final ResourceProvider provider;
	private final ResourceConverter<T> converter;
	private final ResourceFilter filter;

	/**
	 * Creates a new ResourceLoader with the specified components.
	 *
	 * @param provider  The resource provider for discovery and reading
	 * @param converter The converter for parsing resources
	 * @param filter    The filter to apply during resource discovery
	 */
	public ResourceLoader(ResourceProvider provider, ResourceConverter<T> converter, ResourceFilter filter) {
		this.provider = Objects.requireNonNull(provider, "provider cannot be null");
		this.converter = Objects.requireNonNull(converter, "converter cannot be null");
		this.filter = Objects.requireNonNull(filter, "filter cannot be null");
	}

	/**
	 * Creates a new ResourceLoader with the default JSON filter.
	 * For backward compatibility with existing code.
	 *
	 * @param provider  The resource provider
	 * @param converter The converter
	 */
	public ResourceLoader(ResourceProvider provider, ResourceConverter<T> converter) {
		this(provider, converter, ResourceFilter.JSON);
	}

	/**
	 * Loads all resources found within a given root path using the new ResourcePath API.
	 *
	 * @param rootPath  The root path to start resource discovery from
	 * @param recursive If true, loading will include all subdirectories
	 * @return A Stream of successfully loaded and converted resources
	 */
	public Stream<T> load(ResourcePath rootPath, boolean recursive) {
		return provider.list(rootPath, recursive, filter)
				.parallel()
				.map(this::tryLoadResource)
				.flatMap(Optional::stream);
	}

	/**
	 * Loads all resources found within a given root path.
	 * It lists all files using the ResourceProvider, then attempts to read and convert each one.
	 * <p>
	 * This is the legacy API maintained for backward compatibility.
	 *
	 * @param rootPath  The root identifier to start the resource discovery from.
	 * @param recursive If true, loading will include all subdirectories.
	 * @return A Stream of successfully loaded and converted resources.
	 */
	public Stream<T> load(OpenIdentifier rootPath, boolean recursive) {
		return load(ResourcePath.directory(rootPath.namespace(), rootPath.path()), recursive);
	}

	/**
	 * Loads a single resource by its path.
	 *
	 * @param path The path to the resource
	 * @return Optional containing the loaded resource, or empty if not found or conversion failed
	 */
	public Optional<T> loadSingle(ResourcePath path) {
		return tryLoadResource(path);
	}

	/**
	 * Loads a single resource by its identifier.
	 *
	 * @param identifier The identifier of the resource
	 * @return Optional containing the loaded resource, or empty if not found or conversion failed
	 */
	public Optional<T> loadSingle(OpenIdentifier identifier) {
		return tryLoadResource(ResourcePath.fromIdentifier(identifier));
	}

	private Optional<T> tryLoadResource(ResourcePath path) {
		Optional<InputStream> streamOpt = provider.read(path);
		if (streamOpt.isEmpty()) {
			LOGGER.trace("Resource not found: {}", path);
			return Optional.empty();
		}

		try (InputStream stream = streamOpt.get()) {
			return converter.convert(stream, path.toIdentifier());
		} catch (Exception e) {
			LOGGER.error("Failed to load resource {}: {}", path, e.getMessage());
			return Optional.empty();
		}
	}

	/**
	 * Legacy method for loading by OpenIdentifier.
	 */
	private Optional<T> tryLoadResource(OpenIdentifier id) {
		return tryLoadResource(ResourcePath.fromIdentifier(id));
	}

	/**
	 * Creates a new builder for constructing ResourceLoader instances.
	 *
	 * @param <T> The type of resource to load
	 * @return A new builder instance
	 */
	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	/**
	 * Builder for constructing ResourceLoader instances with fluent API.
	 *
	 * @param <T> The type of resource to load
	 */
	public static class Builder<T> {
		private ResourceProvider provider;
		private ResourceConverter<T> converter;
		private ResourceFilter filter = ResourceFilter.JSON;

		private Builder() {
		}

		/**
		 * Sets the resource provider.
		 *
		 * @param provider The provider to use
		 * @return This builder
		 */
		public Builder<T> provider(ResourceProvider provider) {
			this.provider = provider;
			return this;
		}

		/**
		 * Sets the resource converter.
		 *
		 * @param converter The converter to use
		 * @return This builder
		 */
		public Builder<T> converter(ResourceConverter<T> converter) {
			this.converter = converter;
			return this;
		}

		/**
		 * Sets the resource filter.
		 *
		 * @param filter The filter to use
		 * @return This builder
		 */
		public Builder<T> filter(ResourceFilter filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Builds the ResourceLoader.
		 *
		 * @return A new ResourceLoader instance
		 * @throws NullPointerException if provider or converter is not set
		 */
		public ResourceLoader<T> build() {
			Objects.requireNonNull(provider, "provider is required");
			Objects.requireNonNull(converter, "converter is required");
			return new ResourceLoader<>(provider, converter, filter);
		}
	}
}
