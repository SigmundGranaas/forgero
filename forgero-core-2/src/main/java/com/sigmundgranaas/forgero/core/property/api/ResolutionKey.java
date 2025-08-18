package com.sigmundgranaas.forgero.core.property.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * A type-safe key representing a specific kind of resolved data.
 * The key contains a unique identifier and a "phantom" type parameter {@code <R>} that
 * represents the final, resolved result type. It is primarily used by a {@link DataTypeEngine}
 * to identify itself and its output, which is crucial for caching mechanisms within a {@link Resolver}.
 *
 * @param id  The unique identifier for this data type (e.g., "forgero:attributes").
 * @param <R> The type of the result this key will produce (e.g., {@code AttributeQueryResult}).
 */
public record ResolutionKey<R>(OpenIdentifier id) {
}
