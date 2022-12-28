package com.sigmundgranaas.forgeroforge.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgeroforge.recipe.customrecipe.RecipeTypes;
import net.minecraft.util.Identifier;

public interface RecipeWrapper {
    Identifier getRecipeID();

    RecipeTypes getRecipeType();

    JsonObject getRecipe();
}
