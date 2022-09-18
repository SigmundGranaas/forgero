package com.sigmundgranaas.forgero.item.items.testutil;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.Ingredient;

import static com.sigmundgranaas.forgero.item.items.testutil.ToolParts.HANDLE;
import static com.sigmundgranaas.forgero.item.items.testutil.ToolParts.PICKAXE_HEAD;

public class Tools {
    public static Composite IRON_PICKAXE = Composite.builder()
            .addIngredient(Ingredient.of(HANDLE))
            .addIngredient(Ingredient.of(PICKAXE_HEAD))
            .type(Types.PICKAXE)
            .build();
}
