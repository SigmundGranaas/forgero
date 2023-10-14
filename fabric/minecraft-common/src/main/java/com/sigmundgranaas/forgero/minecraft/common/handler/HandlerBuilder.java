package com.sigmundgranaas.forgero.minecraft.common.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.Forgero;

public class HandlerBuilder {
	public static <T> List<T> buildHandlerFromJson(JsonElement element, String key, Function<JsonElement, Optional<T>> builder) {
		List<T> handlers = new ArrayList<>();
		if (element.isJsonObject() && element.getAsJsonObject().has(key)) {
			JsonElement keyElement = element.getAsJsonObject().get(key);
			if (keyElement.isJsonObject() && element.getAsJsonObject().has(key)) {
				var object = element.getAsJsonObject();
				builder.apply(object.get(key)).ifPresent(handlers::add);
			} else if (keyElement.isJsonArray()) {
				for (JsonElement arrayElement : keyElement.getAsJsonArray()) {
					builder.apply(arrayElement).ifPresent(handlers::add);
				}
			}
		}

		if (handlers.isEmpty()) {
			Forgero.LOGGER.warn("Encountered empty handler from structure: {}", element);
		}
		return handlers;
	}
}
