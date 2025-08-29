package com.sigmundgranaas.forgero.utility.resource.loader.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An abstraction for a source of resources, like a file system or a JAR's resource path.
 * This allows the ResourceLoader to be independent of where the data comes from.
 */
public interface ResourceProvider {
	/**
	 * @return A set of all currently loaded namespaces (e.g., "minecraft", "forgero").
	 */
	Set<String> getNamespaces();

	/**
	 * Lists all resource identifiers within a given path.
	 *
	 * @param path      The directory path to list resources from, relative to the provider's root.
	 * @param recursive If true, lists resources in all subdirectories as well.
	 * @return A Stream of full resource identifiers found within the path.
	 */
	Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive);

	/**
	 * Reads a specific resource and provides its content as an InputStream.
	 *
	 * @param identifier The full identifier of the resource to read.
	 * @return An Optional containing an InputStream for the resource, or empty if the resource cannot be found or read.
	 */
	Optional<InputStream> read(OpenIdentifier identifier);
}
