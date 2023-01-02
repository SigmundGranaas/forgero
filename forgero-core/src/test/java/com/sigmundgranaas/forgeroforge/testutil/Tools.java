package com.sigmundgranaas.forgeroforge.testutil;

import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Ingredient;
import com.sigmundgranaas.forgero.core.state.slot.EmptySlot;

import java.util.Set;

public class Tools {
    public static Composite IRON_PICKAXE = Composite.builder()
            .addIngredient(Ingredient.of(ToolParts.HANDLE))
            .addIngredient(Ingredient.of(ToolParts.PICKAXE_HEAD))
            .addUpgrade(new EmptySlot(0, Types.BINDING, "", Set.of(Category.UTILITY)))
            .type(Types.PICKAXE)
            .build();
}
