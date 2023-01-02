package com.sigmundgranaas.forgero.core.model;

public interface Converter<T, R> {
    T convert(R target);
}
