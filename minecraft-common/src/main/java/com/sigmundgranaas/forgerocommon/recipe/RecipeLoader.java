package com.sigmundgranaas.forgerocommon.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgerocommon.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgerocommon.recipe.implementation.RecipeLoaderImpl;

import java.util.Map;

public interface RecipeLoader {

    RecipeLoader INSTANCE = RecipeLoaderImpl.getInstance();

    Map<RecipeTypes, JsonObject> loadRecipeTemplates();
}
