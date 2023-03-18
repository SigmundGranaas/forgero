package com.sigmundgranaas.forgero.minecraft.common.recipe;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

public interface ForgeroRecipeSerializer {
	Identifier getIdentifier();

	RecipeSerializer<?> getSerializer();
}
