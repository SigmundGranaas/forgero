package com.sigmundgranaas.forgero.core.registry;

import java.util.Collection;
import java.util.function.Supplier;

public interface Registry<T> {
    Collection<T> entries();


    Supplier<T> register(T value);
}
