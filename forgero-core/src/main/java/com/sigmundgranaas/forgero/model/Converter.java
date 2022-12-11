package com.sigmundgranaas.forgero.model;

public interface Converter<T, R> {
    T convert(R target);
}
