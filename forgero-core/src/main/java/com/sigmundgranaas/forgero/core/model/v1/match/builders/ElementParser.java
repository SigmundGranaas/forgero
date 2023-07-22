package com.sigmundgranaas.forgero.core.model.v1.match.builders;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ElementParser {

	public static Optional<JsonObject> fromIdentifiedElement(JsonElement element) {
		if (element.isJsonObject()) {
			var object = element.getAsJsonObject();
			return Optional.of(object).filter(jsonObject -> jsonObject.has("type"));
		}
		return Optional.empty();
	}

	public static Optional<String> fromString(JsonElement element) {
		if (element.isJsonPrimitive()) {
			return Optional.of(element.getAsJsonPrimitive().getAsString());
		}
		return Optional.empty();
	}
}
