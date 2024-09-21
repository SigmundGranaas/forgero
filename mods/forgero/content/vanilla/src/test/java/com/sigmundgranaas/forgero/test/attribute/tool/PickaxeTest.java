package com.sigmundgranaas.forgero.test.attribute.tool;

import static com.sigmundgranaas.forgero.test.util.StateHelper.state;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Rarity;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.test.util.AttributeTester;
import com.sigmundgranaas.forgero.test.util.ForgeroPackageTest;
import org.junit.jupiter.api.Test;

/**
 * All vanilla tools should stay within some distance to their minecraft counterparts
 */
public class PickaxeTest extends ForgeroPackageTest {

	@Test
	void testOakPickaxe() {
		AttributeTester.tester(state("forgero:oak-pickaxe"))
				.add(Durability.KEY, 50, 10)
				.add(MiningSpeed.KEY, 2, 1)
				.add(Rarity.KEY, 20, 5)
				.add(AttackDamage.KEY, 2, 0.5)
				.add(AttackSpeed.KEY, -2.0, 0.5)
				.run();
	}

	@Test
	void testIronPickaxe() {
		AttributeTester.tester(state("forgero:iron-pickaxe"))
				.add(Durability.KEY, 250, 50)
				.add(MiningSpeed.KEY, 6, 1)
				.add(Rarity.KEY, 40, 10)
				.add(AttackDamage.KEY, 4, 0.5)
				.add(AttackSpeed.KEY, -2.0, 0.5)
				.run();
	}

	@Test
	void testGoldPickaxe() {
		AttributeTester.tester(state("forgero:gold-pickaxe"))
				.add(Durability.KEY, 41, 10)
				.add(MiningSpeed.KEY, 12, 1)
				.add(Rarity.KEY, 60, 10)
				.add(AttackDamage.KEY, 2, 0.5)
				.add(AttackSpeed.KEY, -2.0, 0.5)
				.run();
	}

	@Test
	void testDiamondPickaxe() {
		AttributeTester.tester(state("forgero:diamond-pickaxe"))
				.add(Durability.KEY, 1500, 300)
				.add(MiningSpeed.KEY, 8, 1)
				.add(Rarity.KEY, 80, 10)
				.add(AttackDamage.KEY, 5, 0.5)
				.add(AttackSpeed.KEY, -2.0, 0.5)
				.run();
	}

	@Test
	void testNetheritePickaxe() {
		AttributeTester.tester(state("forgero:netherite-pickaxe"))
				.add(Durability.KEY, 2100, 300)
				.add(MiningSpeed.KEY, 9, 1)
				.add(Rarity.KEY, 110, 10)
				.add(AttackDamage.KEY, 6, 1.5)
				.add(AttackSpeed.KEY, -2.0, 0.5)
				.run();
	}

	@Test
	void testDiamondPickaxeIronHandle() {
		var pickaxe = Construct.builder()
				.id("forgero:diamond-pickaxe")
				.addIngredient(state("forgero:iron-handle"))
				.addIngredient(state("forgero:diamond-pickaxe_head"))
				.build();

		AttributeTester.tester(pickaxe)
				.add(Durability.KEY, 1600, 200)
				.add(MiningSpeed.KEY, 9, 1)
				.add(Rarity.KEY, 100, 20)
				.add(AttackDamage.KEY, 6, 1.5)
				.add(AttackSpeed.KEY, -2.0, 0.4)
				.run();
	}
}
