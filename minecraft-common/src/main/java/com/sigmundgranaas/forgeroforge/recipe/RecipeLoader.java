package com.sigmundgranaas.forgeroforge.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgeroforge.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgeroforge.recipe.implementation.RecipeLoaderImpl;

import java.util.Map;

public interface RecipeLoader {

    RecipeLoader INSTANCE = RecipeLoaderImpl.getInstance();

    Map<RecipeTypes, JsonObject> loadRecipeTemplates();
}
