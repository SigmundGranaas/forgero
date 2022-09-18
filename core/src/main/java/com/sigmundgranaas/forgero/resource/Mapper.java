package com.sigmundgranaas.forgero.resource;

@FunctionalInterface
public interface Mapper<T, R> {
    T map(R res);
}
