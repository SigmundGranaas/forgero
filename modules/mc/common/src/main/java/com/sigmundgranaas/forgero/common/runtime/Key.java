package com.sigmundgranaas.forgero.common.runtime;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * A typed token used for storing and retrieving values from a DynamicContext.
 * The type parameter <T> ensures type safety at compile time.
 *
 * @param id  The unique identifier for this key.
 * @param <T> The type of the value this key is associated with.
 */
public record Key<T>(OpenIdentifier id) {
}
