package com.sigmundgranaas.forgero.utility.resource.loader.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An abstraction for a source of resources, like a file system or a JAR's resource path.
 * This allows the ResourceLoader to be independent of where the data comes from.
 * <p>
 * ResourceProvider supports both the legacy {@link OpenIdentifier}-based API and the
 * newer {@link ResourcePath}-based API with configurable filtering. Implementations
 * should override the ResourcePath-based methods for best performance; the OpenIdentifier
 * methods delegate to them by default.
 * <p>
 * Providers can be composed using {@link com.sigmundgranaas.forgero.utility.resource.loader.implementation.CompositeResourceProvider}
 * to aggregate multiple sources with configurable priority and conflict resolution.
 */
public interface ResourceProvider {

	/**
	 * @return A set of all currently loaded namespaces (e.g., "minecraft", "forgero").
	 */
	Set<String> getNamespaces();

	// ========== New ResourcePath-based API ==========

	/**
	 * Lists all resources matching the filter within a given path.
	 * <p>
	 * This is the primary listing method that implementations should override.
	 * The default implementation converts to the legacy API for backward compatibility.
	 *
	 * @param path      The directory path to list resources from.
	 * @param recursive If true, lists resources in all subdirectories as well.
	 * @param filter    The filter to apply to discovered resources.
	 * @return A Stream of matching resource paths.
	 */
	default Stream<ResourcePath> list(ResourcePath path, boolean recursive, ResourceFilter filter) {
		// Default implementation delegates to legacy API
		OpenIdentifier legacyPath = path.toIdentifier();
		return list(legacyPath, recursive)
				.map(ResourcePath::fromIdentifier)
				.filter(filter);
	}

	/**
	 * Lists resources with the default JSON filter.
	 * Convenience method for common use case.
	 *
	 * @param path      The directory path to list resources from.
	 * @param recursive If true, lists resources in all subdirectories.
	 * @return A Stream of JSON resource paths.
	 */
	default Stream<ResourcePath> list(ResourcePath path, boolean recursive) {
		return list(path, recursive, ResourceFilter.JSON);
	}

	/**
	 * Reads a specific resource by its path.
	 * <p>
	 * This is the primary read method that implementations should override.
	 * The default implementation converts to the legacy API for backward compatibility.
	 *
	 * @param path The full path to the resource.
	 * @return An Optional containing an InputStream, or empty if not found.
	 */
	default Optional<InputStream> read(ResourcePath path) {
		return read(path.toIdentifier());
	}

	/**
	 * Checks if a resource exists at the given path.
	 * <p>
	 * Default implementation attempts to read the resource; implementations
	 * may override for more efficient existence checking.
	 *
	 * @param path The path to check.
	 * @return true if the resource exists.
	 */
	default boolean exists(ResourcePath path) {
		return read(path).isPresent();
	}

	// ========== Priority and Metadata ==========

	/**
	 * Returns the priority of this provider for ordering in composite providers.
	 * Higher values mean higher priority (checked first).
	 * <p>
	 * Default priorities by convention:
	 * <ul>
	 *   <li>200+ : Test fixtures and programmatic overrides</li>
	 *   <li>100-199 : User overrides and resource packs</li>
	 *   <li>50-99 : Mod-provided resources</li>
	 *   <li>0-49 : Base/default resources</li>
	 * </ul>
	 *
	 * @return The priority value (default 0).
	 */
	default int priority() {
		return 0;
	}

	/**
	 * Returns a human-readable name for this provider.
	 * Used for debugging and logging.
	 *
	 * @return The provider name.
	 */
	default String name() {
		return getClass().getSimpleName();
	}

	// ========== Legacy OpenIdentifier API (for backward compatibility) ==========

	/**
	 * Lists all resource identifiers within a given path.
	 * <p>
	 * This is the legacy API maintained for backward compatibility.
	 * New implementations should override {@link #list(ResourcePath, boolean, ResourceFilter)} instead.
	 *
	 * @param path      The directory path to list resources from, relative to the provider's root.
	 * @param recursive If true, lists resources in all subdirectories as well.
	 * @return A Stream of full resource identifiers found within the path.
	 */
	Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive);

	/**
	 * Reads a specific resource and provides its content as an InputStream.
	 * <p>
	 * This is the legacy API maintained for backward compatibility.
	 * New implementations should override {@link #read(ResourcePath)} instead.
	 *
	 * @param identifier The full identifier of the resource to read.
	 * @return An Optional containing an InputStream for the resource, or empty if the resource cannot be found or read.
	 */
	Optional<InputStream> read(OpenIdentifier identifier);
}
