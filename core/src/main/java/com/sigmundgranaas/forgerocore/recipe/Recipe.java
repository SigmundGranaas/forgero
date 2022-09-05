package com.sigmundgranaas.forgerocore.recipe;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgerocore.state.Identifiable;


public interface Recipe {
    RecipeType type();

    default int amount() {
        return 1;
    }

    default int tier() {
        return 1;
    }

    Identifiable target();

    ImmutableList<Ingredient> ingredients();
}
