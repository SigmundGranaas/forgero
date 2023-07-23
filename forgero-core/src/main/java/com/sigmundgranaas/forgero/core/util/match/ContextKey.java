package com.sigmundgranaas.forgero.core.util.match;

public class ContextKey<T> {
	private final String key;
	private final Class<T> clazz;

	public ContextKey(String key, Class<T> clazz) {
		this.key = key;
		this.clazz = clazz;
	}

	public static <T> ContextKey<T> of(String key, Class<T> clazz) {
		return new ContextKey<>(key, clazz);
	}

	public String getKey() {
		return key;
	}

	public Class<T> getClazz() {
		return clazz;
	}

	@Override
	public String toString() {
		return key;
	}
}
