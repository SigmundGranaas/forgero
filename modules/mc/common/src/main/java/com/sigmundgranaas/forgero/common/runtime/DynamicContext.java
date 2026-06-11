package com.sigmundgranaas.forgero.common.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A typesafe heterogeneous container for dynamic, runtime data.
 * This object is passed to predicates and the 'apply' method of BakedResult
 * to allow for context-dependent calculations.
 */
public class DynamicContext {
	private static final DynamicContext EMPTY = new DynamicContext(Collections.emptyMap());

	private final Map<Key<?>, Object> data;

	private DynamicContext(Map<Key<?>, Object> data) {
		this.data = data;
	}

	/**
	 * Retrieves a typed value from the context.
	 *
	 * @param key The typed key to look up.
	 * @param <T> The type of the value to retrieve.
	 * @return An Optional containing the value if present and of the correct type, otherwise empty.
	 */
	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(Key<T> key) {
		return Optional.ofNullable((T) data.get(key));
	}

	/**
	 * A convenience builder for creating a context.
	 */
	public static class Builder {
		private final Map<Key<?>, Object> data = new HashMap<>();

		public <T> Builder put(Key<T> key, T value) {
			data.put(key, value);
			return this;
		}

		public DynamicContext build() {
			return new DynamicContext(Collections.unmodifiableMap(data));
		}
	}

	public static DynamicContext empty() {
		return EMPTY;
	}
}
