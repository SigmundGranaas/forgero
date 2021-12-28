package com.sigmundgranaas.forgero.recipe;

import java.util.List;

public class RecipeCollectionImpl implements RecipeCollection {
    private static RecipeCollectionImpl INSTANCE;
    private final RecipeCreator recipeCreator;

    public RecipeCollectionImpl(RecipeCreator recipeCreator) {
        this.recipeCreator = recipeCreator;
    }

    public static RecipeCollectionImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RecipeCollectionImpl(RecipeCreator.INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public List<RecipeWrapper> getRecipes() {
        return recipeCreator.createRecipes();
    }

    @Override
    public List<ForgeroRecipeSerializerTypes> getRecipeTypes() {
        return List.of(new ToolRecipe.ToolRecipeSerializer(), new ToolWithBindingRecipe.ToolWithBindingRecipeSerializer());
    }
}
