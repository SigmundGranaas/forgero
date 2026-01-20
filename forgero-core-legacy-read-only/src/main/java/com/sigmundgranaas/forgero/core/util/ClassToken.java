package com.sigmundgranaas.forgero.core.util;


import java.lang.reflect.Type;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class ClassToken<T> extends TypeToken<T> {
	private final Class<T> clazz;

	public ClassToken(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public boolean isInstance(Object instance) {
		return clazz.isInstance(instance);
	}

	@Override
	public T cast(Object instance) {
		return clazz.cast(instance);
	}

	@Override
	public <R> boolean isAssignableFrom(Class<? extends R> targetClass) {
		return clazz.isAssignableFrom(targetClass);
	}

	@Override
	public Type type() {
		return clazz;
	}
}
