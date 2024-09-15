package com.sigmundgranaas.forgero.generator.impl.recipe.validation;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.generator.impl.IdentifiedJson;

import static com.sigmundgranaas.forgero.core.Forgero.LOGGER;
import static com.sigmundgranaas.forgero.predicate.util.JsonUtils.prettyPrintJson;

public class RecipeValidator {
	private final ResultValidator resultValidator;
	private final ShapedRecipeValidator shapedRecipeValidator;
	private final ShapelessRecipeValidator shapelessRecipeValidator;

	public RecipeValidator() {
		this.resultValidator = new ResultValidator();
		this.shapedRecipeValidator = new ShapedRecipeValidator();
		this.shapelessRecipeValidator = new ShapelessRecipeValidator();
	}

	public boolean validateRecipe(IdentifiedJson identifiedJson) {
		JsonObject json = identifiedJson.json();
		boolean isValid = resultValidator.validateResult(json);

		if (json.has("type")) {
			String type = json.get("type").getAsString();
			if ("minecraft:crafting_shaped".equals(type)) {
				isValid &= shapedRecipeValidator.validate(json);
			} else if ("minecraft:crafting_shapeless".equals(type)) {
				isValid &= shapelessRecipeValidator.validate(json);
			} else if ("forgero:state_crafting_recipe".equals(type)) {
				isValid &= shapedRecipeValidator.validate(json);
			} else if ("forgero:schematic_part_crafting".equals(type)) {
				isValid &= shapelessRecipeValidator.validate(json);
			} else {
				// No validation for other types of recipes yet.
			}
		} else {
			LOGGER.error("Missing recipe type for recipe: {}", identifiedJson.id());
			isValid = false;
		}

		if(!isValid){
			LOGGER.error("Found error in the following recipe: \n {}", prettyPrintJson(json.toString()));
			LOGGER.error("The error was found in a recipe generated from this template: \n {}", prettyPrintJson(identifiedJson.template().toString()));
		}
		return isValid;
	}
}
