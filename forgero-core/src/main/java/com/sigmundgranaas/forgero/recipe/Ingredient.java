package com.sigmundgranaas.forgero.recipe;

public interface Ingredient {
    IngredientType ingredientType();

    String ingredient();

    int amount();
}
