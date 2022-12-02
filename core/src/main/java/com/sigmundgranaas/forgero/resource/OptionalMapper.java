package com.sigmundgranaas.forgero.resource;

import java.util.Optional;

@FunctionalInterface
public interface OptionalMapper<T, R> {
    Optional<T> map(R res);
}