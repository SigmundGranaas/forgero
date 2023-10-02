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
public class SwordTest extends ForgeroPackageTest {
	@Test
	void testOakSword() {
		AttributeTester.tester(state("forgero:oak-sword"))
				.add(Durability.KEY, 50, 10)
				.add(MiningSpeed.KEY, 2, 1)
				.add(Rarity.KEY, 20, 5)
				.add(AttackDamage.KEY, 3.5, 0.5)
				.add(AttackSpeed.KEY, -2.1, 0.5)
				.run();
	}

	@Test
	void testIronSword() {
		AttributeTester.tester(state("forgero:iron-sword"))
				.add(Durability.KEY, 250, 50)
				.add(MiningSpeed.KEY, 6, 1)
				.add(Rarity.KEY, 40, 10)
				.add(AttackDamage.KEY, 6, 0.5)
				.add(AttackSpeed.KEY, -2.1, 0.5)
				.run();
	}

	@Test
	void testGoldSword() {
		AttributeTester.tester(state("forgero:gold-sword"))
				.add(Durability.KEY, 41, 10)
				.add(MiningSpeed.KEY, 12, 1)
				.add(Rarity.KEY, 60, 10)
				.add(AttackDamage.KEY, 4, 0.5)
				.add(AttackSpeed.KEY, -2.1, 0.5)
				.run();
	}

	@Test
	void testDiamondSword() {
		AttributeTester.tester(state("forgero:diamond-sword"))
				.add(Durability.KEY, 1500, 300)
				.add(MiningSpeed.KEY, 8, 1)
				.add(Rarity.KEY, 80, 10)
				.add(AttackDamage.KEY, 7, 0.5)
				.add(AttackSpeed.KEY, -2.1, 0.5)
				.run();
	}

	@Test
	void testNetheriteSword() {
		AttributeTester.tester(state("forgero:netherite-sword"))
				.add(Durability.KEY, 2100, 300)
				.add(MiningSpeed.KEY, 9, 1)
				.add(Rarity.KEY, 110, 10)
				.add(AttackDamage.KEY, 8, 1.5)
				.add(AttackSpeed.KEY, -2.1, 0.5)
				.run();
	}

	@Test
	void testDiamondSwordIronHandle() {
		var pickaxe = Construct.builder()
				.id("forgero:diamond-sword")
				.addIngredient(state("forgero:iron-handle"))
				.addIngredient(state("forgero:diamond-sword_blade"))
				.build();

		AttributeTester.tester(pickaxe)
				.add(Durability.KEY, 1600, 200)
				.add(MiningSpeed.KEY, 9, 1)
				.add(Rarity.KEY, 100, 20)
				.add(AttackDamage.KEY, 7, 1.5)
				.add(AttackSpeed.KEY, -2.1, 0.3)
				.run();
	}
}
