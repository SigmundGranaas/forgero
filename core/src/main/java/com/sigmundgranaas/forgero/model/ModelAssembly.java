package com.sigmundgranaas.forgero.model;

@FunctionalInterface
public interface ModelAssembly<T> {
    T convert();
}
