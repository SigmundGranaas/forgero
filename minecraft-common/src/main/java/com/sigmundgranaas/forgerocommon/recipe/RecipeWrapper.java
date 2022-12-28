package com.sigmundgranaas.forgerocommon.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgerocommon.recipe.customrecipe.RecipeTypes;
import net.minecraft.util.Identifier;

public interface RecipeWrapper {
    Identifier getRecipeID();

    RecipeTypes getRecipeType();

    JsonObject getRecipe();
}
