package com.sigmundgranaas.forgero.minecraft.common.conversion;

public interface Converter<T, R> {
	R convert(T value);
}
