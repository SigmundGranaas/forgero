package com.sigmundgranaas.forgero.recipe.implementation;

import com.sigmundgranaas.forgero.recipe.*;

import java.util.ArrayList;
import java.util.List;

public class RecipeCollectionImpl implements RecipeCollection {
    private static RecipeCollectionImpl INSTANCE;
    private final RecipeCreator recipeCreator;
    private List<RecipeWrapper> recipes = new ArrayList<>();

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
        if (recipes.isEmpty()) {
            recipes = recipeCreator.createRecipes();
        }
        return recipes;
    }

    @Override
    public List<ForgeroRecipeSerializer> getRecipeTypes() {
        return List.of(ToolRecipe.ToolRecipeSerializer.INSTANCE, ToolWithBindingRecipe.ToolWithBindingRecipeSerializer.INSTANCE, SecondaryMaterialToolPartUpgradeRecipe.Serializer.INSTANCE, GemUpgradeRecipe.Serializer.INSTANCE);
    }
}
