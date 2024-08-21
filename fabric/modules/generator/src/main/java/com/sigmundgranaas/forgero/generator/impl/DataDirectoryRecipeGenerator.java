package com.sigmundgranaas.forgero.generator.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.sigmundgranaas.forgero.generator.impl.recipe.validation.RecipeValidator;

import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataDirectoryRecipeGenerator {
		private final StringReplacer replacer;
		private final VariableToMapTransformer transformer;
		private final String directory;
		private final ResourceManagerJsonLoader loader;
		private final RecipeValidator recipeValidator;
		private final Predicate<String> isModLoaded;

		public DataDirectoryRecipeGenerator(StringReplacer replacer, VariableToMapTransformer transformer,
											String directory, ResourceManagerJsonLoader loader, Predicate<String> isModLoaded) {
			this.replacer = replacer;
			this.transformer = transformer;
			this.directory = directory;
			this.loader = loader;
			this.isModLoaded = isModLoaded;
			this.recipeValidator = new RecipeValidator();
		}

		public Collection<IdentifiedJson> generate() {
			return loader.load(directory)
					.parallelStream()
					.filter(this::checkDependencies)
					.flatMap(this::convertToIdentifiedJson)
					.filter(recipeValidator::validateRecipe)
					.collect(Collectors.toList());
		}

		private boolean checkDependencies(JsonObject object){
			if(object.get("dependencies") == null){
				return true;
			}else{
				JsonArray dependencies = object.get("dependencies").getAsJsonArray();
				for(JsonElement dependency : dependencies){
					if(!isModLoaded.test(dependency.getAsString())){
						return false;
					}
				}
			}
			return true;
		}

	private Stream<IdentifiedJson> convertToIdentifiedJson(JsonObject object) {
		return transformer.transformStateMap(object.getAsJsonObject("variables"))
				.stream()
				.map(variables -> createRecipe(copy(object), variables));
	}

	private IdentifiedJson createRecipe(JsonObject template, Map<String, Object> variableMap) {
		Identifier id = new Identifier(replacer.applyReplacements(template.get("identifier").getAsString(), variableMap));
		JsonObject recipe = new Gson().fromJson(replacer.applyReplacements(template.toString(), variableMap), JsonObject.class);
		recipe.remove("identifier");
		recipe.remove("generator_type");
		recipe.remove("variables");

		return new IdentifiedJson(id, recipe, template);
	}

	private JsonObject copy(JsonObject object) {
		return new Gson().fromJson(object.toString(), JsonObject.class);
	}
}
