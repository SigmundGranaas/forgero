package com.sigmundgranaas.forgero.utility.resource.loader.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.io.InputStream;
import java.util.Optional;

/**
 * A functional interface for converting the raw content of a resource into a specific object type.
 *
 * @param <T> The type of the resource object to produce.
 */
@FunctionalInterface
public interface ResourceConverter<T> {
	/**
	 * Converts an InputStream and its corresponding identifier into a resource object.
	 *
	 * @param stream The InputStream containing the resource's raw data.
	 * @param id     The unique identifier of the resource.
	 * @return An Optional containing the converted resource, or empty if the conversion fails or the resource should be skipped.
	 */
	Optional<T> convert(InputStream stream, OpenIdentifier id);
}
