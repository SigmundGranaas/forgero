package com.sigmundgranaas.forgero.item.items.testutil;

import com.sigmundgranaas.forgerocore.state.Composite;
import com.sigmundgranaas.forgerocore.state.Ingredient;

import static com.sigmundgranaas.forgero.item.items.testutil.ToolParts.HANDLE;
import static com.sigmundgranaas.forgero.item.items.testutil.ToolParts.PICKAXE_HEAD;

public class Tools {
    public static Composite IRON_PICKAXE = Composite.builder()
            .add(Ingredient.of(HANDLE))
            .add(Ingredient.of(PICKAXE_HEAD))
            .type(Types.PICKAXE)
            .build();
}
