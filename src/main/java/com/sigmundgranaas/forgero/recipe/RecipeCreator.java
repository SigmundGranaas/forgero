package com.sigmundgranaas.forgero.recipe;

import com.sigmundgranaas.forgero.recipe.implementation.RecipeCreatorImpl;

import java.util.List;

public interface RecipeCreator {
    RecipeCreator INSTANCE = RecipeCreatorImpl.getInstance();

    List<RecipeWrapper> createRecipes();
}
