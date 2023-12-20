package com.sigmundgranaas.forgero.core.property.v2.feature;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.handler.HandlerBuilderRegistry;
import com.sigmundgranaas.forgero.core.util.TypeToken;

public class HandlerBuilder {
	public static HandlerBuilder DEFAULT = new HandlerBuilder(new HandlerBuilderRegistry());
	private final HandlerBuilderRegistry registry;

	public HandlerBuilder(HandlerBuilderRegistry registry) {
		this.registry = registry;
	}

	public static <T> JsonBuilder<T> fromObjectOrStringDefaulted(Class<T> clazz, String type, Function<JsonObject, T> baseBuilder, Supplier<T> defaultSupplier) {
		return fromObjectOrStringDefaulted(TypeToken.of(clazz), type, baseBuilder, defaultSupplier);
	}

	public static <T> JsonBuilder<T> fromObjectOrStringDefaulted(TypeToken<T> clazz, String type, Function<JsonObject, T> baseBuilder, Supplier<T> defaultSupplier) {
		return new JsonBuilder<T>() {
			@Override
			public Optional<T> build(JsonElement element) {
				if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
					return fromStringOptional(type, defaultSupplier).apply(element.getAsString());
				} else if (element.isJsonObject()) {
					return Optional.of(baseBuilder.apply(element.getAsJsonObject()));
				}
				return Optional.empty();
			}

			@Override
			public TypeToken<T> getTargetClass() {
				return clazz;
			}
		};
	}


	public static <T> JsonBuilder<T> fromObject(TypeToken<T> clazz, Function<JsonObject, T> baseBuilder) {
		return fromObjectOptional(clazz, (JsonObject object) -> Optional.of(baseBuilder.apply(object)));
	}

	public static <T> JsonBuilder<T> fromObject(Class<T> clazz, Function<JsonObject, T> baseBuilder) {
		return fromObjectOptional(TypeToken.of(clazz), (JsonObject object) -> Optional.of(baseBuilder.apply(object)));
	}

	public static <T> JsonBuilder<T> fromString(TypeToken<T> clazz, Function<String, Optional<T>> baseBuilder) {
		return new JsonBuilder<T>() {
			@Override
			public Optional<T> build(JsonElement element) {
				return (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) ? baseBuilder.apply(element.getAsString()) : Optional.empty();
			}

			@Override
			public TypeToken<T> getTargetClass() {
				return clazz;
			}
		};
	}

	public static <T> JsonBuilder<T> fromString(Class<T> clazz, Function<String, Optional<T>> baseBuilder) {
		return fromString(TypeToken.of(clazz), baseBuilder);
	}

	public static <T> Function<String, Optional<T>> fromStringOptional(String type, Supplier<T> supplier) {
		return (String string) -> string.equals(type) ? Optional.of(supplier.get()) : Optional.empty();
	}

	public static <T> JsonBuilder<T> fromObjectOptional(TypeToken<T> clazz, Function<JsonObject, Optional<T>> baseBuilder) {
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
			public TypeToken<T> getTargetClass() {
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
		} else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
			return registry.get(key, element.getAsString())
					.flatMap(builder -> builder.build(element));
		}
		return Optional.empty();
	}
}
