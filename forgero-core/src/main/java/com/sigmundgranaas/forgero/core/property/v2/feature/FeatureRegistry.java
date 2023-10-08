package com.sigmundgranaas.forgero.core.property.v2.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonObject;

public class FeatureRegistry {
	private static final Map<FeatureKey<?>, FeatureBuilder<?>> builders = new HashMap<>();
	private static final Map<String, Class<? extends Feature>> idMap = new HashMap<>();


	public static <T extends Feature> Optional<T> of(FeatureKey<T> key, JsonObject object) {
		return Optional.ofNullable(builders.get(key))
				.flatMap(builder -> builder.build(object))
				.map(key.clazz()::cast);
	}

	public static Optional<Feature> of(JsonObject object) {
		if (object.has("type")) {
			String type = object.get("type").getAsString();
			Optional<Class<? extends Feature>> clazz = Optional.ofNullable(idMap.get(type));
			if (clazz.isPresent()) {
				return clazz.map(zz -> new FeatureKey<>(type, zz))
						.flatMap(key -> Optional.ofNullable(builders.get(key)))
						.flatMap(builder -> builder.build(object))
						.map(clazz.get()::cast);
			}
		}
		return Optional.empty();
	}

	public static <T extends Feature> void register(FeatureKey<T> key, FeatureBuilder<T> builder) {
		idMap.put(key.type(), key.clazz());
		builders.put(key, builder);
	}
}
