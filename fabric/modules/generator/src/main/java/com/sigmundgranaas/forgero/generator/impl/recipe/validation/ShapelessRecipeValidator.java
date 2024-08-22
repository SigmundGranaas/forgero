package com.sigmundgranaas.forgero.generator.impl.recipe.validation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import static com.sigmundgranaas.forgero.core.Forgero.LOGGER;

public class ShapelessRecipeValidator {
	private final IngredientValidator ingredientValidator;

	public ShapelessRecipeValidator() {
		this.ingredientValidator = new IngredientValidator();
	}

	public boolean validate(JsonObject json) {
		if (!json.has("ingredients")) {
			LOGGER.error("Missing ingredients in shapeless recipe");
			return false;
		}

		JsonArray ingredients = json.getAsJsonArray("ingredients");
		return ingredientValidator.validateIngredients(ingredients);
	}
}
