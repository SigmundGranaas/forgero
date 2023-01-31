package com.sigmundgranaas.forgero.minecraft.common.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeWrapperImpl;
import net.minecraft.util.Identifier;

public interface RecipeWrapper {
    static RecipeWrapper of(Identifier id, JsonObject recipe, RecipeTypes type) {
        return new RecipeWrapperImpl(id, recipe, type);
    }

    Identifier getRecipeID();

    RecipeTypes getRecipeType();

    JsonObject getRecipe();
}
