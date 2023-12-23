package com.sigmundgranaas.forgero.generator.impl.converter.forgero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;
import com.sigmundgranaas.forgero.core.state.State;

public class ForgeroTypeVariableConverter implements RankableConverter<JsonElement, Collection<?>> {
	private final Function<String, List<State>> stateProvider;

	public ForgeroTypeVariableConverter(Function<String, List<State>> stateProvider) {
		this.stateProvider = stateProvider;
	}

	@Override
	public Collection<State> convert(JsonElement entry) {
		var jsonObject = entry.getAsJsonObject();
		JsonElement typeElement = jsonObject.get("type");

		List<State> states;
		if (typeElement.isJsonArray()) {
			states = StreamSupport.stream(typeElement.getAsJsonArray().spliterator(), false)
					.flatMap(type -> stateProvider.apply(type.getAsString()).stream())
					.distinct()
					.collect(Collectors.toList());
		} else {
			String type = typeElement.getAsString();
			states = new ArrayList<>(stateProvider.apply(type));
		}
		if (jsonObject.has("filter")) {
			Set<String> exclusions = parseFilter(jsonObject.getAsJsonArray("filter"));
			states.removeIf(state -> exclusions.contains(state.identifier()));
		}

		return states;
	}

	private Set<String> parseFilter(JsonArray filterArray) {
		return StreamSupport.stream(filterArray.spliterator(), false)
				.map(JsonElement::getAsString)
				.collect(Collectors.toSet());
	}

	@Override
	public boolean matches(JsonElement entry) {
		if (entry.isJsonObject()) {
			var jsonObject = entry.getAsJsonObject();
			return jsonObject.has("type");
		}
		return false;
	}
}
