package com.sigmundgranaas.forgero.conversion;

public interface Converter<T, R> {
	R convert(T value);
}
