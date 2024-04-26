package com.sigmundgranaas.forgero.test.util;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.api.identity.DefaultRules;
import com.sigmundgranaas.forgero.core.api.identity.ModificationRuleRegistry;
import com.sigmundgranaas.forgero.core.api.identity.sorting.SortingRule;
import com.sigmundgranaas.forgero.core.api.identity.sorting.SortingRuleRegistry;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;

public class StateHelper {
	public static State state(String id) {
		return ForgeroStateRegistry.stateFinder().find(id).orElseThrow();
	}

	public static void ruleSetup() {
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
	}
}
