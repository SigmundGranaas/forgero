package com.sigmundgranaas.forgerocommon.conversion;

public interface Converter<T, R> {
    R convert(T value);
}
