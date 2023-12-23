package com.sigmundgranaas.forgero.minecraft.common.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeWrapperImpl;

import net.minecraft.util.Identifier;

public interface RecipeWrapper {
	static RecipeWrapper of(Identifier id, JsonObject recipe) {
		return new RecipeWrapperImpl(id, recipe);
	}

	Identifier getRecipeID();

	JsonObject getRecipe();
}
