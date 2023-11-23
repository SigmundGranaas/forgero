package com.sigmundgranaas.forgero.core.handler;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerBuilderRegistry {
	private static final Map<ClassKey<?>, Map<String, JsonBuilder<?>>> REGISTRY = new ConcurrentHashMap<>();

	public static <T extends Object> void register(ClassKey<T> type, String key, JsonBuilder<? extends T> builder) {
		REGISTRY.computeIfAbsent(type, k -> new ConcurrentHashMap<>()).put(key, builder);
	}

	public static <T> RegisterBuilder<T> builder(String key, JsonBuilder<T> builder) {
		return new RegisterBuilder<T>(key, builder);
	}

	@SuppressWarnings("unchecked")
	private static <T> Optional<JsonBuilder<T>> safeCast(JsonBuilder<?> builder, ClassKey<T> type) {
		try {
			if (type.clazz().isAssignableFrom(builder.getTargetClass())) {
				return Optional.of((JsonBuilder<T>) builder);
			}
		} catch (ClassCastException ignored) {
			Forgero.LOGGER.error("Could not cast builder to type: " + type.clazz().getName());
		}
		return Optional.empty();
	}

	public <T> Optional<JsonBuilder<T>> get(ClassKey<T> type, String key) {
		return Optional.ofNullable(REGISTRY.get(type))
				.flatMap(map -> Optional.ofNullable(map.get(key)))
				.flatMap(builder -> safeCast(builder, type));
	}

	public record RegisterBuilder<R>(String key, JsonBuilder<R> builder) {
		public <T> RegisterBuilder<R> register(ClassKey<T> type) {
			safeCast(builder, type).ifPresent(tJsonBuilder -> HandlerBuilderRegistry.register(type, key, tJsonBuilder));
			return this;
		}
	}
}


