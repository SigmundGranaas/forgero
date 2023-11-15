package com.sigmundgranaas.forgero.core.handler;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;

public class HandlerBuilderRegistry {
	private static final Map<ClassKey<?>, Map<String, JsonBuilder<?>>> REGISTRY = new ConcurrentHashMap<>();

	public static <T extends Object> void register(ClassKey<T> type, String key, JsonBuilder<? extends T> builder) {
		REGISTRY.computeIfAbsent(type, k -> new ConcurrentHashMap<>()).put(key, builder);
	}

	@SuppressWarnings("unchecked")
	private static <T> Optional<JsonBuilder<T>> safeCast(JsonBuilder<?> builder, ClassKey<T> type) {
		try {
			if (type.clazz().isAssignableFrom(builder.getTargetClass())) {
				return Optional.of((JsonBuilder<T>) builder);
			}
		} catch (ClassCastException ignored) {
		}
		return Optional.empty();
	}

	public <T> Optional<JsonBuilder<T>> get(ClassKey<T> type, String key) {
		return Optional.ofNullable(REGISTRY.get(type))
				.flatMap(map -> Optional.ofNullable(map.get(key)))
				.flatMap(builder -> safeCast(builder, type));
	}
}


