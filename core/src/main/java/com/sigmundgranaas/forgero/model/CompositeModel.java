package com.sigmundgranaas.forgero.model;

import java.util.List;

public class CompositeModel implements ModelAble {
    private List<ModelAble> models;

    public CompositeModel(List<ModelAble> models) {
        this.models = models;
    }

    public List<ModelAble> getModels() {
        return models;
    }

    public void setModels(List<ModelAble> models) {
        this.models = models;
    }

    @Override
    public <T> T convert(Converter<T, ModelAble> converter) {
        return converter.convert(this);
    }
}
