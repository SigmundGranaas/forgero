package com.sigmundgranaas.forgero.generator.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.generator.impl.recipe.validation.RecipeValidator;

import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RecipeGenerator {
		private final StringReplacer replacer;
		private final VariableToMapTransformer transformer;
		private final RecipeValidator recipeValidator;
		private final Predicate<String> isModLoaded;
		private final Gson gson;

		public RecipeGenerator(StringReplacer replacer, VariableToMapTransformer transformer,
                               Predicate<String> isModLoaded) {
			this.replacer = replacer;
			this.transformer = transformer;
			this.isModLoaded = isModLoaded;
			this.recipeValidator = new RecipeValidator();
			this.gson = new Gson();
		}
	public RecipeGenerator(StringReplacer replacer, VariableToMapTransformer transformer) {
		this.replacer = replacer;
		this.transformer = transformer;
		this.isModLoaded = (string) -> true;
		this.recipeValidator = new RecipeValidator();
		this.gson = new Gson();
	}

		public List<IdentifiedJson> generateRecipeFrom(JsonObject template) {
			if(checkDependencies(template)){
				return convertToIdentifiedJson(template);
			}else{
				return Collections.emptyList();
			}
		}

		private boolean checkDependencies(JsonObject object) {
			boolean result;
			if(object.get("dependencies") == null) {
				result = true;
			} else {
				JsonArray dependencies = object.get("dependencies").getAsJsonArray();
				result = dependencies.asList().stream()
						.map(JsonElement::getAsString)
						.allMatch(isModLoaded);
			}
			return result;
		}

	private List<IdentifiedJson> convertToIdentifiedJson(JsonObject object) {
		return	transformer.transformStateMap(object.getAsJsonObject("variables"))
				.parallelStream()
				.map(variables -> createRecipe(object, variables)).toList();
	}

	private IdentifiedJson createRecipe(JsonObject template, Map<String, Object> variableMap) {
		String idString = replacer.applyReplacements(template.get("identifier").getAsString(), variableMap);
		Identifier id = new Identifier(idString);

		String jsonString = replacer.applyReplacements(template.toString(), variableMap);
		JsonObject recipe = gson.fromJson(jsonString, JsonObject.class);

		recipe.remove("identifier");
		recipe.remove("generator_type");
		recipe.remove("variables");

		return new IdentifiedJson(id, recipe, recipe);
	}
}
