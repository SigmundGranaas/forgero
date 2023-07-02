package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation;

import com.sigmundgranaas.forgero.minecraft.common.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeCollection;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeCreator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.*;

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
				RepairKitRecipe.RepairKitRecipeSerializer.INSTANCE,
				StateUpgradeShapelessRecipe.StateUpgradeShapelessRecipeSerializer.INSTANCE);
	}
}
