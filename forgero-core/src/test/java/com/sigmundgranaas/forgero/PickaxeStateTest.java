package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.testutil.Tools;
import com.sigmundgranaas.forgero.testutil.Types;
import com.sigmundgranaas.forgero.testutil.Upgrades;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PickaxeStateTest {
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
