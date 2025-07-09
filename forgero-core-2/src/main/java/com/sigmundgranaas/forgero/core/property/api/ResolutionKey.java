package com.sigmundgranaas.forgero.core.property.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * A type-safe key used to request a specific kind of data from the {@link Resolver}.
 * The key contains a unique identifier and a "phantom" type parameter {@code <R>} that
 * represents the final, resolved result type. This allows the resolver's `resolve` method
 * to be generic while providing a type-safe return value to the caller.
 *
 * @param id  The unique identifier for this data type (e.g., "forgero:attributes").
 * @param <R> The type of the result this key will produce (e.g., {@code AttributeQueryResult}).
 */
public record ResolutionKey<R>(OpenIdentifier id) {
}
