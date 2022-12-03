package com.sigmundgranaas.forgero.testutil;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.Ingredient;

public class Tools {
    public static Composite IRON_PICKAXE = Composite.builder()
            .addIngredient(Ingredient.of(ToolParts.HANDLE))
            .addIngredient(Ingredient.of(ToolParts.PICKAXE_HEAD))
            .type(Types.PICKAXE)
            .build();
}
