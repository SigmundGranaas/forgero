package com.sigmundgranaas.forgero.recipe;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

public interface RecipeWrapper {
    Identifier getRecipeID();

    RecipeTypes getRecipeType();

    JsonObject getRecipe();
}
