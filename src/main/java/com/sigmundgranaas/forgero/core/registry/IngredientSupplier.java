package com.sigmundgranaas.forgero.core.registry;

import com.sigmundgranaas.forgero.core.state.Ingredient;

import java.util.Optional;

@FunctionalInterface
public interface IngredientSupplier {
    Optional<Ingredient> get(String id);
}
