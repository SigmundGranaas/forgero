package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.state.Identifiable;

import java.util.Optional;
import java.util.function.Supplier;

public interface IdentifiableRegistry<T extends Identifiable> extends Registry<T> {
    default Optional<? extends Supplier<T>> find(Identifiable id) {
        return find(id.identifier());
    }

    Optional<? extends Supplier<T>> find(String id);

    boolean contains(String id);

    default boolean contains(Identifiable id) {
        return contains(id.identifier());
    }
}
