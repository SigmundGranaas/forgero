package com.sigmundgranaas.forgero.core.recipe;

public interface Ingredient {
	IngredientType ingredientType();

	String ingredient();

	int amount();
}
