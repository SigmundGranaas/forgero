package com.sigmundgranaas.forgero.recipe.implementation;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.List;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.service.StateService;

public class RecipeUtils {
	public static JsonObject ingredientsToJsonEntry(IngredientData data) {
		var object = new JsonObject();
		if (!data.id().equals(EMPTY_IDENTIFIER)) {
			var tag = StateService.INSTANCE.getMapper().stateToTag(data.id());
			if (tag.isPresent()) {
				object.addProperty("tag", tag.get().toString());
			} else {
				object.addProperty("item", data.id());
			}
		} else {
			object.addProperty("tag", "forgero:" + data.type().toLowerCase(Locale.ENGLISH));
		}
		return object;
	}

	public static String ingredientToString(IngredientData data) {
		if (!data.id().equals(EMPTY_IDENTIFIER)) {
			return data.id();
		} else {
			return data.type().toLowerCase(Locale.ENGLISH);
		}
	}

	public static String ingredientsToRecipeId(List<IngredientData> ingredients) {
		return String.join(ELEMENT_SEPARATOR, ingredients.stream().map(RecipeUtils::ingredientToString).toList());
	}
}
