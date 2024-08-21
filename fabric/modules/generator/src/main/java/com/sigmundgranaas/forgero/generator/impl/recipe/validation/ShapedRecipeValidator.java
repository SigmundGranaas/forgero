package com.sigmundgranaas.forgero.generator.impl.recipe.validation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static com.sigmundgranaas.forgero.core.Forgero.LOGGER;

public class ShapedRecipeValidator {
	private final IngredientValidator ingredientValidator;

	public ShapedRecipeValidator() {
		this.ingredientValidator = new IngredientValidator();
	}

	public boolean validate(JsonObject json) {
		if (!json.has("pattern") || !json.has("key")) {
			LOGGER.error("Missing pattern or key in shaped recipe");
			return false;
		}

		JsonArray pattern = json.getAsJsonArray("pattern");
		JsonObject key = json.getAsJsonObject("key");

		for (JsonElement row : pattern) {
			String rowStr = row.getAsString();
			for (char c : rowStr.toCharArray()) {
				if (c != ' ' && !key.has(String.valueOf(c))) {
					LOGGER.error("Invalid key '{}' in pattern", c);
					return false;
				}
			}
		}

		return ingredientValidator.validateIngredients(key);
	}
}
