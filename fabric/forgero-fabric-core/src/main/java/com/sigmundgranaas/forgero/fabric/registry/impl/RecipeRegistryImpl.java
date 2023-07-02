package com.sigmundgranaas.forgero.fabric.registry.impl;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.fabric.registry.RecipeRegistry;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeCollection;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public record RecipeRegistryImpl(
		RecipeCollection collection) implements RecipeRegistry {
	private static RecipeRegistry INSTANCE;

	public static RecipeRegistry getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new RecipeRegistryImpl(RecipeCollection.INSTANCE);
		}
		return INSTANCE;
	}

	@Override
	public void registerRecipes(Map<Identifier, JsonElement> map) {
		collection.getRecipes().forEach(recipeWrapper -> map.put(recipeWrapper.getRecipeID(), recipeWrapper.getRecipe()));
	}

	@Override
	public void registerRecipeSerializers() {
		collection.getRecipeTypes().forEach(serializer -> Registry.register(Registry.RECIPE_SERIALIZER, serializer.getIdentifier(), serializer.getSerializer()));
	}
}
