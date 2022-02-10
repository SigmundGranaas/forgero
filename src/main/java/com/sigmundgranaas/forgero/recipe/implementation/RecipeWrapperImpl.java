package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import net.minecraft.util.Identifier;

public record RecipeWrapperImpl(Identifier id,
                                JsonObject recipe, RecipeTypes type) implements RecipeWrapper {

    @Override
    public Identifier getRecipeID() {
        return id;
    }

    @Override
    public RecipeTypes getRecipeType() {
        return type;
    }

    @Override
    public JsonObject getRecipe() {
        return recipe;
    }
}
