package com.sigmundgranaas.forgero.generator.impl;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class DataDirectoryRecipeGenerator {
	private final StringReplacer replacer;
	private final VariableToMapTransformer transformer;
	private final String directory;
	private final ResourceManagerJsonLoader loader;

	public DataDirectoryRecipeGenerator(StringReplacer replacer, VariableToMapTransformer transformer, String directory, ResourceManagerJsonLoader loader) {
		this.replacer = replacer;
		this.transformer = transformer;
		this.directory = directory;
		this.loader = loader;
	}

	public Collection<IdentifiedJson> generate() {
		return loader.load(directory)
				.stream()
				.flatMap(this::convertToIdentifiedJson)
				.filter(this::validateRecipe)
				.collect(Collectors.toList());
	}

	private boolean validateRecipe(IdentifiedJson identifiedJson) {
		if (identifiedJson.json().has("result")) {
			JsonObject result = identifiedJson.json().getAsJsonObject("result");
			if (result.has("item")) {
				Identifier item = new Identifier(result.get("item").getAsString());
				return Registry.ITEM.containsId(item);
			}
		}
		return false;
	}


	private Stream<IdentifiedJson> convertToIdentifiedJson(JsonObject object) {
		return transformer.transformStateMap(object.getAsJsonObject("variables"))
				.stream()
				.map(variables -> createRecipe(copy(object), variables));
	}


	private IdentifiedJson createRecipe(JsonObject template, Map<String, Object> variableMap) {
		Identifier id = new Identifier(replacer.applyReplacements(template.get("identifier").getAsString(), variableMap));
		template.remove("identifier");
		template.remove("generator_type");
		template.remove("variables");


		template = new Gson().fromJson(replacer.applyReplacements(template.toString(), variableMap), JsonObject.class);
		return new IdentifiedJson(id, template);
	}

	private JsonObject copy(JsonObject object) {
		return new Gson().fromJson(object.toString(), JsonObject.class);
	}
}
