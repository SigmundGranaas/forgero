package com.sigmundgranaas.forgerocommon.recipe;

import com.sigmundgranaas.forgerocommon.recipe.implementation.RecipeCollectionImpl;

import java.util.List;

public interface RecipeCollection {
    RecipeCollection INSTANCE = RecipeCollectionImpl.getInstance();

    List<RecipeWrapper> getRecipes();

    List<ForgeroRecipeSerializer> getRecipeTypes();
}

