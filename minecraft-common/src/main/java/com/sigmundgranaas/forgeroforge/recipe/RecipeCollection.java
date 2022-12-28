package com.sigmundgranaas.forgeroforge.recipe;

import com.sigmundgranaas.forgeroforge.recipe.implementation.RecipeCollectionImpl;

import java.util.List;

public interface RecipeCollection {
    RecipeCollection INSTANCE = RecipeCollectionImpl.getInstance();

    List<RecipeWrapper> getRecipes();

    List<ForgeroRecipeSerializer> getRecipeTypes();
}

