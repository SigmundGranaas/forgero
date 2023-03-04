package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;

import java.util.List;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public class RecipeUtils {
	public static JsonObject ingredientsToJsonEntry(IngredientData data) {
		var object = new JsonObject();
		if (!data.id().equals(EMPTY_IDENTIFIER)) {
			object.addProperty("item", data.id());
		} else {
			object.addProperty("tag", "forgero:" + data.type().toLowerCase());
		}
		return object;
	}

	public static String ingredientToString(IngredientData data) {
		if (!data.id().equals(EMPTY_IDENTIFIER)) {
			return data.id();
		} else {
			return data.type().toLowerCase();
		}
	}

	public static String ingredientsToRecipeId(List<IngredientData> ingredients) {
		return String.join(ELEMENT_SEPARATOR, ingredients.stream().map(RecipeUtils::ingredientToString).toList());
	}
}
