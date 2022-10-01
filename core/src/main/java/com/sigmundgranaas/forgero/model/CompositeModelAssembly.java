package com.sigmundgranaas.forgero.model;

import java.util.List;

public class CompositeModelAssembly<T> implements ModelAssembly<T> {
    private final Converter<T, CompositeModelAssembly<T>> converter;
    private List<ModelAssembly<T>> models;

    public CompositeModelAssembly(List<ModelAssembly<T>> models, Converter<T, CompositeModelAssembly<T>> converter) {
        this.converter = converter;
    }

    @Override
    public T convert() {
        return converter.convert(this);
    }

    public List<ModelAssembly<T>> getModels() {
        return models;
    }
}
