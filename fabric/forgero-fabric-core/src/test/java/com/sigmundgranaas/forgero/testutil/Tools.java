package com.sigmundgranaas.forgero.testutil;

import java.util.Set;

import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.Ingredient;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.EmptySlot;

public class Tools {
	public static Construct IRON_PICKAXE = Construct.builder()
			.addIngredient(Ingredient.of(ToolParts.HANDLE))
			.addIngredient(Ingredient.of(ToolParts.PICKAXE_HEAD))
			.addUpgrade(new EmptySlot(0, Types.BINDING, "", Set.of(Category.UTILITY)))
			.type(Types.PICKAXE)
			.build();
}
