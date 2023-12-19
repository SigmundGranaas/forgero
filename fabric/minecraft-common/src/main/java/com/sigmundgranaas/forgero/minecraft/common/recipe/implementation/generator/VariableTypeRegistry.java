package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.recipe.generator.StateVariableType;
import com.sigmundgranaas.forgero.minecraft.common.recipe.generator.StringVariableType;
import com.sigmundgranaas.forgero.minecraft.common.recipe.generator.VariableType;

public class VariableTypeRegistry {
	public static VariableType<?> createVariableType(JsonElement jsonElement, Function<String, List<State>> stateProvider) {
		if (jsonElement.isJsonArray()) {
			List<String> values = new Gson().fromJson(jsonElement, new TypeToken<List<String>>() {
			}.getType());
			return new StringVariableType(values);
		} else if (jsonElement.isJsonObject()) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			if (jsonObject.has("type")) {
				JsonElement typeElement = jsonObject.get("type");

				List<State> states;
				if (typeElement.isJsonArray()) {
					states = StreamSupport.stream(typeElement.getAsJsonArray().spliterator(), false)
							.flatMap(type -> stateProvider.apply(type.getAsString()).stream())
							.collect(Collectors.toList());
				} else {
					String type = typeElement.getAsString();
					states = new ArrayList<>(stateProvider.apply(type));
				}
				if (jsonObject.has("filter")) {
					Set<String> exclusions = parseFilter(jsonObject.getAsJsonArray("filter"));
					states.removeIf(state -> exclusions.contains(state.identifier()));
				}

				return new StateVariableType(states);
			}
		}
		throw new IllegalArgumentException("Unsupported variable type: " + jsonElement);
	}

	private static Set<String> parseFilter(JsonArray filterArray) {
		return StreamSupport.stream(filterArray.spliterator(), false)
				.map(JsonElement::getAsString)
				.collect(Collectors.toSet());
	}
}
