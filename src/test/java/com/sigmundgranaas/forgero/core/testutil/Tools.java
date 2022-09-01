package com.sigmundgranaas.forgero.core.testutil;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Ingredient;

import static com.sigmundgranaas.forgero.core.testutil.ToolParts.HANDLE;
import static com.sigmundgranaas.forgero.core.testutil.ToolParts.PICKAXE_HEAD;

public class Tools {
    public static Composite IRON_PICKAXE = Composite.builder()
            .add(Ingredient.of(HANDLE))
            .add(Ingredient.of(PICKAXE_HEAD))
            .type(Types.PICKAXE)
            .build();
}
