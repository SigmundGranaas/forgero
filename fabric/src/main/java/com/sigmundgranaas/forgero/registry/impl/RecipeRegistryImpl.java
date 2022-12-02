package com.sigmundgranaas.forgero.registry.impl;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.recipe.RecipeCollection;
import com.sigmundgranaas.forgero.registry.RecipeRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public record RecipeRegistryImpl(
        RecipeCollection collection) implements RecipeRegistry {
    private static RecipeRegistry INSTANCE;

    public static RecipeRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RecipeRegistryImpl(RecipeCollection.INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public void registerRecipes(Map<Identifier, JsonElement> map) {
        collection.getRecipes().forEach(recipeWrapper -> map.put(recipeWrapper.getRecipeID(), recipeWrapper.getRecipe()));
    }

    @Override
    public void registerRecipeSerializers() {
        collection.getRecipeTypes().forEach(serializer -> Registry.register(Registry.RECIPE_SERIALIZER, serializer.getIdentifier(), serializer.getSerializer()));
    }
}
