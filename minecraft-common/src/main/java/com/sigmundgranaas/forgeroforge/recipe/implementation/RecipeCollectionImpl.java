package com.sigmundgranaas.forgeroforge.recipe.implementation;

import com.sigmundgranaas.forgeroforge.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgeroforge.recipe.RecipeCollection;
import com.sigmundgranaas.forgeroforge.recipe.RecipeCreator;
import com.sigmundgranaas.forgeroforge.recipe.RecipeWrapper;
import com.sigmundgranaas.forgeroforge.recipe.customrecipe.*;

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
        return List.of(
                GemUpgradeRecipe.Serializer.INSTANCE,
                StateCraftingRecipe.StateCraftingRecipeSerializer.INSTANCE,
                StateUpgradeRecipe.Serializer.INSTANCE,
                SchematicPartRecipe.SchematicPartRecipeSerializer.INSTANCE,
                RepairKitRecipe.RepairKitRecipeSerializer.INSTANCE);
    }
}
