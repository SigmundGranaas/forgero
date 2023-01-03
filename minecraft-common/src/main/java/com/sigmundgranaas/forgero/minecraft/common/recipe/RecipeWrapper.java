package com.sigmundgranaas.forgero.minecraft.common.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import net.minecraft.util.Identifier;

public interface RecipeWrapper {
    Identifier getRecipeID();

    RecipeTypes getRecipeType();

    JsonObject getRecipe();
}
