package com.sigmundgranaas.forgero.model;

public interface ModelTemplate {
    <T> T convert(Converter<T, ModelTemplate> converter);
}
