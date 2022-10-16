package com.sigmundgranaas.forgero.model;

public interface ModelTemplate {
    int order();

    <T> T convert(Converter<T, ModelTemplate> converter);
}
