package com.sigmundgranaas.forgero.core.property.v2.feature;

import java.util.Optional;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.handler.HandlerBuilderRegistry;

public class HandlerBuilder {
	public static HandlerBuilder DEFAULT = new HandlerBuilder(new HandlerBuilderRegistry());
	private final HandlerBuilderRegistry registry;

	public HandlerBuilder(HandlerBuilderRegistry registry) {
		this.registry = registry;
	}

	public static <T> JsonBuilder<T> fromObject(Class<T> clazz, Function<JsonObject, T> baseBuilder) {
		return fromObjectOptional(clazz, (JsonObject object) -> Optional.of(baseBuilder.apply(object)));
	}

	public static <T> JsonBuilder<T> fromObjectOptional(Class<T> clazz, Function<JsonObject, Optional<T>> baseBuilder) {
		return new JsonBuilder<T>() {
			@Override
			public Optional<T> build(JsonElement element) {
				if (element.isJsonObject()) {
					var object = element.getAsJsonObject();
					return baseBuilder.apply(object);
				}
				return Optional.empty();
			}

			@Override
			public Class<T> getTargetClass() {
				return clazz;
			}
		};
	}

	public <T> Optional<T> build(ClassKey<T> key, JsonElement element) {
		if (element.isJsonObject()) {
			var object = element.getAsJsonObject();
			if (object.has("type")) {
				String type = object.get("type").getAsString();
				return registry.get(key, type)
						.flatMap(builder -> builder.build(element));
			}
		}
		return Optional.empty();
	}
}
