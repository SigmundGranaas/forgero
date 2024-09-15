package com.sigmundgranaas.forgero.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.recipe.implementation.RecipeLoaderImpl;

import java.util.Map;

public interface RecipeLoader {

	RecipeLoader INSTANCE = RecipeLoaderImpl.getInstance();

	Map<RecipeTypes, JsonObject> loadRecipeTemplates();
}
