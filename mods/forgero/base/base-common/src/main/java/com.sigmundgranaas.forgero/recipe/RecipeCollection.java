package com.sigmundgranaas.forgero.recipe;

import com.sigmundgranaas.forgero.recipe.implementation.RecipeCollectionImpl;

import java.util.List;

public interface RecipeCollection {
	RecipeCollection INSTANCE = RecipeCollectionImpl.getInstance();

	List<RecipeWrapper> getRecipes();

	List<ForgeroRecipeSerializer> getRecipeTypes();
}

