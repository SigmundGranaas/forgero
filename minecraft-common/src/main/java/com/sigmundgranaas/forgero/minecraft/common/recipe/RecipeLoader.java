package com.sigmundgranaas.forgero.minecraft.common.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeLoaderImpl;

import java.util.Map;

public interface RecipeLoader {

    RecipeLoader INSTANCE = RecipeLoaderImpl.getInstance();

    Map<RecipeTypes, JsonObject> loadRecipeTemplates();
}
