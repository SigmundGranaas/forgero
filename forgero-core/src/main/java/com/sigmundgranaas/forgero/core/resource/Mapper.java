package com.sigmundgranaas.forgero.core.resource;

@FunctionalInterface
public interface Mapper<T, R> {
    T map(R res);
}
