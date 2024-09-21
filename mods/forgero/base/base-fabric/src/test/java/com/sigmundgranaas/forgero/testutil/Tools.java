package com.sigmundgranaas.forgero.testutil;

import java.util.Set;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.api.v0.identity.DefaultRules;
import com.sigmundgranaas.forgero.core.api.v0.identity.ModificationRuleRegistry;
import com.sigmundgranaas.forgero.core.api.v0.identity.sorting.SortingRule;
import com.sigmundgranaas.forgero.core.api.v0.identity.sorting.SortingRuleRegistry;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.Ingredient;
import com.sigmundgranaas.forgero.core.state.NameCompositor;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.EmptySlot;
import com.sigmundgranaas.forgero.core.type.Type;

public class Tools {
	public static NameCompositor defaultCompositor() {
		SortingRuleRegistry sorting = SortingRuleRegistry.staticRegistry();
		sorting.registerRule("forgero:schematic", SortingRule.of(Type.SCHEMATIC, 20));
		sorting.registerRule("forgero:material", SortingRule.of(Type.MATERIAL, 10));
		sorting.registerRule("forgero:part", SortingRule.of(Type.PART, 30));

		ModificationRuleRegistry modification = ModificationRuleRegistry.staticRegistry();

		modification.registerRule("forgero:schematic", DefaultRules.schematic.build());
		modification.registerRule("forgero:handle", DefaultRules.handle.build());
		modification.registerRule("forgero:pickaxe", DefaultRules.pickaxe.build());
		modification.registerRule("forgero:sword", DefaultRules.sword.build());
		modification.registerRule("forgero:hoe", DefaultRules.hoe.build());
		modification.registerRule("forgero:axe", DefaultRules.axe.build());
		modification.registerRule("forgero:shovel", DefaultRules.shovel.build());
		return new NameCompositor(modification, sorting);
	}

	public static Supplier<Construct> IRON_PICKAXE = () -> Construct.builder()
			.addIngredient(Ingredient.of(ToolParts.PICKAXE_HEAD))
			.addIngredient(Ingredient.of(ToolParts.HANDLE))
			.addUpgrade(new EmptySlot(0, Type.BINDING, "", Set.of(Category.UTILITY)))
			.type(Type.PICKAXE)
			.compositor(defaultCompositor())
			.build();
}
