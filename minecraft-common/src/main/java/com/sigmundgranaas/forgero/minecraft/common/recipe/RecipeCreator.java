package com.sigmundgranaas.forgero.minecraft.common.recipe;

import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeCreatorImpl;

import java.util.List;

public interface RecipeCreator {
    RecipeCreator INSTANCE = RecipeCreatorImpl.getInstance();

    List<RecipeWrapper> createRecipes();
}
