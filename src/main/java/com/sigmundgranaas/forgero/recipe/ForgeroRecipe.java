package com.sigmundgranaas.forgero.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;

public interface ForgeroRecipe {
    String getID();
    RecipeTypes getType();
    JsonObject getData();
}
