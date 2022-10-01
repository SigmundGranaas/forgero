package com.sigmundgranaas.forgero.model;

public interface ModelAble {
    <T> T convert(Converter<T, ModelAble> converter);
}
