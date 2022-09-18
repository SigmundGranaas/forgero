package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.state.Ingredient;

import java.util.Optional;

@FunctionalInterface
public interface IngredientSupplier {
    Optional<Ingredient> get(String id);
}
