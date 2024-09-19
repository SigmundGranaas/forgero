package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.core.api.v0.identity.DefaultRules;
import com.sigmundgranaas.forgero.core.api.v0.identity.ModificationRuleRegistry;
import com.sigmundgranaas.forgero.core.api.v0.identity.sorting.SortingRule;
import com.sigmundgranaas.forgero.core.api.v0.identity.sorting.SortingRuleRegistry;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.testutil.Tools;
import com.sigmundgranaas.forgero.testutil.Types;
import com.sigmundgranaas.forgero.testutil.Upgrades;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PickaxeStateTest {
	@BeforeAll
	public static void setup() {
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

	@Test
	void testCreatePickaxeState() {
		var pick = Tools.IRON_PICKAXE;
		Assertions.assertEquals("iron-pickaxe", pick.name());
	}

	@Test
	void textPickaxeProperties() {
		var pick = Tools.IRON_PICKAXE;
		Assertions.assertEquals(4, pick.getProperties().size());
	}

	@Test
	void testPickaxeType() {
		var pick = Tools.IRON_PICKAXE;
		Assertions.assertTrue(pick.test(Types.TOOL));
	}

	@Test
	void testPickaxeTypeWoodTrue() {
		var pick = Tools.IRON_PICKAXE;
		Assertions.assertTrue(pick.test(Types.WOOD));
	}

	@Test
	void testPickaxeTypeFailForRandom() {
		var pick = Tools.IRON_PICKAXE;
		Assertions.assertFalse(pick.test(Types.RANDOM));
	}

	@Test
	void testUpgradePickaxeIsNewInstance() {
		var pick = Tools.IRON_PICKAXE;
		Assertions.assertNotEquals(pick, pick.upgrade(Upgrades.BINDING));
	}

	@Test
	void testUpgradesApply() {
		var pick = Tools.IRON_PICKAXE.upgrade(Upgrades.BINDING);
		Assertions.assertTrue(pick.stream().applyAttribute(Durability.KEY) > Tools.IRON_PICKAXE.stream().applyAttribute(Durability.KEY));
	}
}
