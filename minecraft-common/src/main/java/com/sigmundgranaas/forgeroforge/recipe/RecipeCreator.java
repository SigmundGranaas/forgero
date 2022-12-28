package com.sigmundgranaas.forgeroforge.recipe;

import com.sigmundgranaas.forgeroforge.recipe.implementation.RecipeCreatorImpl;

import java.util.List;

public interface RecipeCreator {
    RecipeCreator INSTANCE = RecipeCreatorImpl.getInstance();

    List<RecipeWrapper> createRecipes();
}
