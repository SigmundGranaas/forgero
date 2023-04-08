package com.sigmundgranaas.forgero.minecraft.common.recipe;

import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeCollectionImpl;

import java.util.List;

public interface RecipeCollection {
	RecipeCollection INSTANCE = RecipeCollectionImpl.getInstance();

	List<RecipeWrapper> getRecipes();

	List<ForgeroRecipeSerializer> getRecipeTypes();
}

