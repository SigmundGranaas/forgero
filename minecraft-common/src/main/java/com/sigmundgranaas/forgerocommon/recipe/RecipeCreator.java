package com.sigmundgranaas.forgerocommon.recipe;

import com.sigmundgranaas.forgerocommon.recipe.implementation.RecipeCreatorImpl;

import java.util.List;

public interface RecipeCreator {
    RecipeCreator INSTANCE = RecipeCreatorImpl.getInstance();

    List<RecipeWrapper> createRecipes();
}
