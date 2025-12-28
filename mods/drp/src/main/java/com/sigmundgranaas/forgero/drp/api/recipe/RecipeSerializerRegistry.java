package com.sigmundgranaas.forgero.drp.api.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.drp.impl.serialization.DefaultRecipeSerializers;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for recipe serializers.
 * <p>
 * This registry allows plugins to add support for custom recipe types.
 * Serializers are looked up by the builder class type.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Register a custom serializer
 * RecipeSerializerRegistry.register(new MyCustomRecipeSerializer());
 *
 * // Serialize a recipe builder
 * Optional<JsonObject> json = RecipeSerializerRegistry.serialize(myBuilder);
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>This registry is thread-safe. Registrations can happen from any thread,
 * including during mod initialization.</p>
 */
public final class RecipeSerializerRegistry {

	private static final Map<Class<?>, RecipeSerializer<?>> SERIALIZERS = new ConcurrentHashMap<>();

	static {
		// Register default serializers
		DefaultRecipeSerializers.registerDefaults();
	}

	private RecipeSerializerRegistry() {
		// Prevent instantiation
	}

	/**
	 * Registers a recipe serializer.
	 * <p>
	 * If a serializer for the same builder type is already registered,
	 * it will be replaced.
	 *
	 * @param serializer The serializer to register
	 * @param <T>        The builder type
	 */
	public static <T extends RecipeBuilder<?>> void register(RecipeSerializer<T> serializer) {
		SERIALIZERS.put(serializer.getBuilderType(), serializer);
	}

	/**
	 * Gets the serializer for a specific builder type.
	 *
	 * @param builderType The builder class
	 * @param <T>         The builder type
	 * @return The serializer, if registered
	 */
	@SuppressWarnings("unchecked")
	public static <T extends RecipeBuilder<?>> Optional<RecipeSerializer<T>> get(Class<T> builderType) {
		// Check exact match first
		RecipeSerializer<?> serializer = SERIALIZERS.get(builderType);
		if (serializer != null) {
			return Optional.of((RecipeSerializer<T>) serializer);
		}

		// Check for interface/superclass matches
		for (Map.Entry<Class<?>, RecipeSerializer<?>> entry : SERIALIZERS.entrySet()) {
			if (entry.getKey().isAssignableFrom(builderType)) {
				return Optional.of((RecipeSerializer<T>) entry.getValue());
			}
		}

		return Optional.empty();
	}

	/**
	 * Serializes a recipe builder using the appropriate registered serializer.
	 *
	 * @param builder The recipe builder to serialize
	 * @param <T>     The builder type
	 * @return The serialized JSON, or empty if no serializer is registered
	 */
	@SuppressWarnings("unchecked")
	public static <T extends RecipeBuilder<?>> Optional<JsonObject> serialize(T builder) {
		Class<T> builderType = (Class<T>) builder.getClass();

		// Try exact class first
		RecipeSerializer<?> serializer = SERIALIZERS.get(builderType);
		if (serializer != null) {
			return Optional.of(((RecipeSerializer<T>) serializer).serialize(builder));
		}

		// Try interfaces and superclasses
		for (Map.Entry<Class<?>, RecipeSerializer<?>> entry : SERIALIZERS.entrySet()) {
			if (entry.getKey().isAssignableFrom(builderType)) {
				return Optional.of(((RecipeSerializer<T>) entry.getValue()).serialize(builder));
			}
		}

		return Optional.empty();
	}

	/**
	 * Checks if a serializer is registered for the given builder type.
	 *
	 * @param builderType The builder class
	 * @return true if a serializer is registered
	 */
	public static boolean hasSerializer(Class<? extends RecipeBuilder<?>> builderType) {
		if (SERIALIZERS.containsKey(builderType)) {
			return true;
		}

		for (Class<?> registered : SERIALIZERS.keySet()) {
			if (registered.isAssignableFrom(builderType)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Unregisters a serializer by builder type.
	 * <p>
	 * Primarily useful for testing.
	 *
	 * @param builderType The builder class to unregister
	 */
	public static void unregister(Class<? extends RecipeBuilder<?>> builderType) {
		SERIALIZERS.remove(builderType);
	}

	/**
	 * Gets the number of registered serializers.
	 * <p>
	 * Primarily useful for testing.
	 *
	 * @return The number of registered serializers
	 */
	public static int size() {
		return SERIALIZERS.size();
	}
}
