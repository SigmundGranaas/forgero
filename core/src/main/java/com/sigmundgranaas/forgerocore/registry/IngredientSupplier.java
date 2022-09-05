package com.sigmundgranaas.forgerocore.registry;

import com.sigmundgranaas.forgerocore.state.Ingredient;

import java.util.Optional;

@FunctionalInterface
public interface IngredientSupplier {
    Optional<Ingredient> get(String id);
}
