package com.sigmundgranaas.forgero.generator.impl.recipe.validation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import com.google.gson.JsonObject;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Map;

import static com.sigmundgranaas.forgero.core.Forgero.LOGGER;


public class IngredientValidator {

	public boolean validateIngredients(JsonElement ingredientsElement) {
		if (ingredientsElement.isJsonObject()) {
			JsonObject ingredients = ingredientsElement.getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : ingredients.entrySet()) {
				if (!validateIngredient(entry.getValue())) {
					return false;
				}
			}
		} else if (ingredientsElement.isJsonArray()) {
			JsonArray ingredients = ingredientsElement.getAsJsonArray();
			for (JsonElement ingredient : ingredients) {
				if (!validateIngredient(ingredient)) {
					return false;
				}
			}
		} else {
			LOGGER.error("Invalid ingredients format");
			return false;
		}
		return true;
	}

	private boolean validateIngredient(JsonElement ingredient) {
		if (ingredient.isJsonObject()) {
			JsonObject ingredientObj = ingredient.getAsJsonObject();
			if (ingredientObj.has("item")) {
				return validateItem(ingredientObj);
			} else if (ingredientObj.has("tag")) {
				return validateTag(ingredientObj);
			} else {
				LOGGER.error("Invalid ingredient format: {}", ingredientObj);
				return false;
			}
		} else {
			LOGGER.error("Invalid ingredient format: {}", ingredient);
			return false;
		}
	}

	private boolean validateItem(JsonObject ingredientObj) {
		Identifier item = new Identifier(ingredientObj.get("item").getAsString());
		if (!Registries.ITEM.containsId(item)) {
			LOGGER.error("Invalid ingredient item: {}", item);
			return false;
		}
		return true;
	}

	private boolean validateTag(JsonObject ingredientObj) {
		String tagString = ingredientObj.get("tag").getAsString();
		if (tagString.isEmpty()) {
			LOGGER.error("Empty tag in ingredient");
			return false;
		}

		// Tags are not loaded when this is loaded, so we cannot validate the tag contents here.

		return true;
	}
}
