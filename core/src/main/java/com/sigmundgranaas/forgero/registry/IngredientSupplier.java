package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.state.State;

import java.util.Optional;

@FunctionalInterface
public interface IngredientSupplier {
    Optional<State> get(String id);
}
