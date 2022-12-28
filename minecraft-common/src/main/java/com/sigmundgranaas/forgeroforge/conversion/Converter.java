package com.sigmundgranaas.forgeroforge.conversion;

public interface Converter<T, R> {
    R convert(T value);
}
