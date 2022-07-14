package com.sigmundgranaas.forgero.core.constructable;

import java.util.Optional;

public interface Convertable<T> {
    default Optional<T> convert(){
        return Optional.empty();
    }
}
