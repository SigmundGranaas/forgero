package com.sigmundgranaas.forgero.core.util;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class TypeToken<T> {
	private final Type type;

	protected TypeToken() {
		Type superclass = getClass().getGenericSuperclass();
		if (superclass instanceof Class) {
			throw new RuntimeException("Missing type parameter.");
		}
		this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
	}

	public static <T> TypeToken<T> of(Class<T> clazz) {
		return new ClassToken<>(clazz);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof TypeToken<?> && ((TypeToken<?>) o).type().equals(type());
	}

	@Override
	public int hashCode() {
		return type().hashCode();
	}

	public boolean isInstance(Object instance) {
		if (type() instanceof Class<?> clazz) {
			return clazz.isInstance(instance);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public T cast(Object instance) {
		if (type() instanceof Class<?>) {
			Class<T> clazz = (Class<T>) type();
			if (clazz.isInstance(instance)) {
				return clazz.cast(instance);
			}
		}
		throw new ClassCastException("Cannot cast object to " + type().getTypeName());
	}

	public <R> boolean isAssignableFrom(Class<? extends R> targetClass) {
		if (type() instanceof Class<?> clazz) {
			return clazz.isAssignableFrom(targetClass);
		}
		return false;
	}

	public <R> boolean isAssignableFrom(TypeToken<? extends R> targetClass) {
		if (targetClass.type() instanceof Class<?> clazz) {
			return isAssignableFrom(clazz);
		}
		return false;
	}
}
