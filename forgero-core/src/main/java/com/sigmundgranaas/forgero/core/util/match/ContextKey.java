package com.sigmundgranaas.forgero.core.util.match;

/**
 * A class that represents a key for context metadata, incorporating both the key's name and
 * the type of value it corresponds to.
 *
 * @param <T> The type of the value associated with this key.
 */
@SuppressWarnings("ClassCanBeRecord")
public class ContextKey<T> {
	private final String key;
	private final Class<T> clazz;

	/**
	 * Constructor that initializes the key and clazz fields.
	 *
	 * @param key   The name of the key.
	 * @param clazz The Class object representing the type of the value associated with this key.
	 */
	public ContextKey(String key, Class<T> clazz) {
		this.key = key;
		this.clazz = clazz;
	}

	/**
	 * Static factory method that creates a new ContextKey instance.
	 *
	 * @param <T>   The type of the value associated with the key.
	 * @param key   The name of the key.
	 * @param clazz The Class object representing the type of the value associated with this key.
	 * @return A new instance of ContextKey with the given key name and value type.
	 */
	public static <T> ContextKey<T> of(String key, Class<T> clazz) {
		return new ContextKey<>(key, clazz);
	}

	/**
	 * Getter method for the key field.
	 *
	 * @return The name of the key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Getter method for the clazz field.
	 *
	 * @return The Class object representing the type of the value associated with this key.
	 */
	public Class<T> getClazz() {
		return clazz;
	}

	/**
	 * Returns a string representation of the ContextKey, which is the key's name.
	 *
	 * @return The name of the key.
	 */
	@Override
	public String toString() {
		return key;
	}
}
