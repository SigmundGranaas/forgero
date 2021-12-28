package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.recipe.RecipeWrapper;
import net.minecraft.util.Identifier;

public class RecipeWrapperImpl implements RecipeWrapper {
    private final Identifier id;
    private final JsonObject recipe;

    public RecipeWrapperImpl(Identifier id, JsonObject recipe) {
        this.id = id;
        this.recipe = recipe;
    }

    @Override
    public Identifier getRecipeID() {
        return id;
    }

    @Override
    public JsonObject getRecipe() {
        return recipe;
    }
}
