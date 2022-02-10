package com.sigmundgranaas.forgero.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import net.minecraft.util.Identifier;

public interface RecipeWrapper {
    Identifier getRecipeID();

    RecipeTypes getRecipeType();

    JsonObject getRecipe();
}
