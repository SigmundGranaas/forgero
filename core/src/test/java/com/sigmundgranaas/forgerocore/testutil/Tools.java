package com.sigmundgranaas.forgerocore.testutil;

import com.sigmundgranaas.forgerocore.state.Composite;
import com.sigmundgranaas.forgerocore.state.Ingredient;

public class Tools {
    public static Composite IRON_PICKAXE = Composite.builder()
            .add(Ingredient.of(ToolParts.HANDLE))
            .add(Ingredient.of(ToolParts.PICKAXE_HEAD))
            .type(Types.PICKAXE)
            .build();
}
