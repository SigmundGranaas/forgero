package com.sigmundgranaas.forgeroforge.test.attribute.tool;

import static com.sigmundgranaas.forgeroforge.test.util.StateHelper.state;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Rarity;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgeroforge.test.util.AttributeTester;
import com.sigmundgranaas.forgeroforge.test.util.ForgeroPackageTest;
import org.junit.jupiter.api.Test;

/**
 * All vanilla tools should stay within some distance to their minecraft counterparts
 */
public class AxeTest extends ForgeroPackageTest {
	@Test
	void testOakAxe() {
		AttributeTester.tester(state("forgero:oak-axe"))
				.add(Durability.KEY, 50, 10)
				.add(MiningSpeed.KEY, 2, 1)
				.add(Rarity.KEY, 20, 5)
				.add(AttackDamage.KEY, 6, 0.5)
				.add(AttackSpeed.KEY, -3.0, 0.5)
				.run();
	}

	@Test
	void testIronAxe() {
		AttributeTester.tester(state("forgero:iron-axe"))
				.add(Durability.KEY, 250, 50)
				.add(MiningSpeed.KEY, 6, 1)
				.add(Rarity.KEY, 40, 10)
				.add(AttackDamage.KEY, 8, 0.5)
				.add(AttackSpeed.KEY, -3.0, 0.5)
				.run();
	}

	@Test
	void testGoldAxe() {
		AttributeTester.tester(state("forgero:gold-axe"))
				.add(Durability.KEY, 41, 10)
				.add(MiningSpeed.KEY, 12, 1)
				.add(Rarity.KEY, 60, 10)
				.add(AttackDamage.KEY, 6, 0.5)
				.add(AttackSpeed.KEY, -3.0, 0.5)
				.run();
	}

	@Test
	void testDiamondAxe() {
		AttributeTester.tester(state("forgero:diamond-axe"))
				.add(Durability.KEY, 1500, 300)
				.add(MiningSpeed.KEY, 8, 1)
				.add(Rarity.KEY, 80, 10)
				.add(AttackDamage.KEY, 9, 0.5)
				.add(AttackSpeed.KEY, -3.0, 0.5)
				.run();
	}

	@Test
	void testNetheriteAxe() {
		AttributeTester.tester(state("forgero:netherite-axe"))
				.add(Durability.KEY, 2100, 300)
				.add(MiningSpeed.KEY, 9, 1)
				.add(Rarity.KEY, 110, 10)
				.add(AttackDamage.KEY, 10, 1.5)
				.add(AttackSpeed.KEY, -3.0, 0.5)
				.run();
	}

	@Test
	void testDiamondAxeIronHandle() {
		var pickaxe = Construct.builder()
				.id("forgero:diamond-axe")
				.addIngredient(state("forgero:iron-handle"))
				.addIngredient(state("forgero:diamond-axe_head"))
				.build();

		AttributeTester.tester(pickaxe)
				.add(Durability.KEY, 1600, 200)
				.add(MiningSpeed.KEY, 9, 1)
				.add(Rarity.KEY, 100, 20)
				.add(AttackDamage.KEY, 8, 1.5)
				.add(AttackSpeed.KEY, -2.8, 0.4)
				.run();
	}
}
