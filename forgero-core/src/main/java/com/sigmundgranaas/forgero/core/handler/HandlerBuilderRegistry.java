package com.sigmundgranaas.forgero.core.handler;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;

/**
 * A registry for handling the association between various handlers {@link ClassKey}s and their corresponding JsonBuilders.
 * It provides functionality to register and retrieve JsonBuilders based on type keys.
 */
public class HandlerBuilderRegistry {
	private static final Map<ClassKey<?>, Map<String, JsonBuilder<?>>> REGISTRY = new ConcurrentHashMap<>();

	/**
	 * Registers a JsonBuilder for a specific type.
	 *
	 * @param type    The class key representing the type for which the builder is being registered.
	 * @param key     A unique string key to identify the builder.
	 * @param builder The JsonBuilder instance to be registered.
	 * @param <T>     The type parameter associated with the class key.
	 */
	public static <T extends Object> void register(ClassKey<T> type, String key, JsonBuilder<? extends T> builder) {
		REGISTRY.computeIfAbsent(type, k -> new ConcurrentHashMap<>()).put(key, builder);
	}

	/**
	 * Creates a RegisterBuilder instance.
	 *
	 * @param key     The key associated with the JsonBuilder.
	 * @param builder The JsonBuilder instance.
	 * @param <T>     The type parameter for the builder.
	 * @return A new RegisterBuilder instance.
	 */
	public static <T> RegisterBuilder<T> builder(String key, JsonBuilder<T> builder) {
		return new RegisterBuilder<T>(key, builder);
	}

	/**
	 * Safely casts a JsonBuilder to a specified type.
	 *
	 * @param builder The JsonBuilder to cast.
	 * @param type    The class key representing the target type.
	 * @param <R>     The generic type of the JsonBuilder.
	 * @param <T>     The target type for the cast.
	 * @return An Optional containing the cast JsonBuilder if successful, or an empty Optional otherwise.
	 */
	@SuppressWarnings("unchecked")
	private static <R, T> Optional<JsonBuilder<T>> safeCast(JsonBuilder<? extends R> builder, ClassKey<T> type) {
		try {
			if (type.clazz().isAssignableFrom(builder.getTargetClass())) {
				return Optional.of((JsonBuilder<T>) builder);
			}
		} catch (ClassCastException ignored) {
			Forgero.LOGGER.error("Could not cast builder to type: " + type.clazz().type().getTypeName());
		}
		return Optional.empty();
	}

	/**
	 * Retrieves a JsonBuilder associated with the specified type and key.
	 *
	 * @param type The class key representing the type.
	 * @param key  The unique key for the JsonBuilder.
	 * @param <T>  The type parameter associated with the class key.
	 * @return An Optional containing the JsonBuilder if found, or an empty Optional otherwise.
	 */
	public <T> Optional<JsonBuilder<T>> get(ClassKey<T> type, String key) {
		return Optional.ofNullable(REGISTRY.get(type))
				.flatMap(map -> Optional.ofNullable(map.get(key)))
				.flatMap(builder -> safeCast(builder, type));
	}

	/**
	 * A record that facilitates the registration of JsonBuilders in the HandlerBuilderRegistry.
	 * It encapsulates the key and the builder, providing a method to register the builder for a specific type.
	 *
	 * @param <R> The type parameter of the JsonBuilder.
	 */
	public record RegisterBuilder<R>(String key, JsonBuilder<? extends R> builder) {
		/**
		 * Registers the encapsulated builder in the HandlerBuilderRegistry for the specified type.
		 *
		 * @param type The class key representing the type for registration.
		 * @param <T>  The type parameter for the registration.
		 * @return The current instance of RegisterBuilder for chaining.
		 */
		public <T> RegisterBuilder<R> register(ClassKey<T> type) {
			if (type.clazz().isAssignableFrom(builder.getTargetClass())) {
				safeCast(builder, type).ifPresent(tJsonBuilder -> HandlerBuilderRegistry.register(type, key, tJsonBuilder));
			}
			return this;
		}
	}
}

