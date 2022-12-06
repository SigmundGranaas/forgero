package com.sigmundgranaas.forgero.testutil;

import com.sigmundgranaas.forgero.property.attribute.Category;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.Ingredient;
import com.sigmundgranaas.forgero.state.slot.EmptySlot;

import java.util.Set;

public class Tools {
    public static Composite IRON_PICKAXE = Composite.builder()
            .addIngredient(Ingredient.of(ToolParts.HANDLE))
            .addIngredient(Ingredient.of(ToolParts.PICKAXE_HEAD))
            .addUpgrade(new EmptySlot(0, Types.BINDING, "", Set.of(Category.UTILITY)))
            .type(Types.PICKAXE)
            .build();
}
