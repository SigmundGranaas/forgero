package com.sigmundgranaas.forgero.core.tags.engine;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TagParser {
	private final Gson gson;

	public TagParser() {
		this.gson = new GsonBuilder()
				.registerTypeAdapter(TagDefinition.class, new TagDefinitionDeserializer())
				.create();
	}

	public TagDefinition parse(String jsonContent) {
		return gson.fromJson(jsonContent, TagDefinition.class);
	}

	private static class TagDefinitionDeserializer implements JsonDeserializer<TagDefinition> {
		@Override
		public TagDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			Set<String> parents = Collections.emptySet();

			if (jsonObject.has("parents")) {
				JsonElement parentsElement = jsonObject.get("parents");
				if (parentsElement.isJsonArray()) {
					parents = StreamSupport.stream(parentsElement.getAsJsonArray().spliterator(), false)
							.map(JsonElement::getAsString)
							.collect(Collectors.toSet());
				}
			} else if (jsonObject.has("parent")) {
				JsonElement parentElement = jsonObject.get("parent");
				if (parentElement.isJsonPrimitive()) {
					parents = Set.of(parentElement.getAsString());
				}
			}
			return new TagDefinition(parents);
		}
	}
}
