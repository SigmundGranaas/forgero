package com.sigmundgranaas.forgerocore.recipe;

public interface Ingredient {
    IngredientType ingredientType();

    String ingredient();

    int amount();
}
