package com.sigmundgranaas.forgero.recipe;

import java.util.List;

public interface RecipeCollection {
    RecipeCollection INSTANCE = RecipeCollectionImpl.getInstance();

    List<RecipeWrapper> getRecipes();

    List<ForgeroRecipeSerializerTypes> getRecipeTypes();
}

