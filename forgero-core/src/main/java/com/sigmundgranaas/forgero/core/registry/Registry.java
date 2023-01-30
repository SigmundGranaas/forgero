package com.sigmundgranaas.forgero.core.registry;

import java.util.Collection;
import java.util.function.Supplier;

public interface Registry<T> {
    Supplier<T> register(T value);

    Collection<T> entries();
}
