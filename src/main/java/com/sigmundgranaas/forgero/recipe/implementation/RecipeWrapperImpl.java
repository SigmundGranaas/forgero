package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.recipe.RecipeTypes;
import com.sigmundgranaas.forgero.recipe.RecipeWrapper;
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
