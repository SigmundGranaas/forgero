package com.sigmundgranaas.forgero.generator.impl.converter;

import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;

public class StringListVariableConverter implements RankableConverter<JsonElement, Collection<?>> {
	@Override
	public Collection<String> convert(JsonElement entry) {
		return new Gson().fromJson(entry, new TypeToken<List<String>>() {
		}.getType());
	}

	@Override
	public boolean matches(JsonElement entry) {
		if (entry.isJsonArray()) {
			return StreamSupport.stream(entry.getAsJsonArray().spliterator(), false)
					.filter(JsonElement::isJsonPrimitive)
					.map(JsonElement::getAsJsonPrimitive)
					.allMatch(JsonPrimitive::isString);
		}
		return false;
	}
}
