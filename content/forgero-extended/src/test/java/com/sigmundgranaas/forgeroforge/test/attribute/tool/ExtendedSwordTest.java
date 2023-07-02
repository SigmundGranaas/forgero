package com.sigmundgranaas.forgeroforge.test.attribute.tool;

import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgeroforge.test.util.AttributeTester;
import com.sigmundgranaas.forgeroforge.test.util.ForgeroPackageTest;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.core.property.AttributeType.*;
import static com.sigmundgranaas.forgeroforge.test.util.StateHelper.state;

/**
 * All vanilla tools should stay within some distance to their minecraft counterparts
 */
public class ExtendedSwordTest extends ForgeroPackageTest {
	@Test
	void testOakShortSword() {
		var sword = Construct.builder()
				.id("forgero:oak-sword")
				.addIngredient(state("forgero:oak-handle"))
				.addIngredient(state("forgero:oak-shortsword_blade"))
				.build();

		AttributeTester.tester(sword)
				.add(DURABILITY, 50, 10)
				.add(MINING_SPEED, 2, 1)
				.add(RARITY, 6, 5)
				.add(ATTACK_DAMAGE, 3, 0.5)
				.add(ATTACK_SPEED, -2.8, 0.5)
				.run();
	}

	@Test
	void testIronSword() {
		var sword = Construct.builder()
				.id("forgero:iron-sword")
				.addIngredient(state("forgero:oak-handle"))
				.addIngredient(state("forgero:iron-shortsword_blade"))
				.build();

		AttributeTester.tester(sword)
				.add(DURABILITY, 250, 50)
				.add(MINING_SPEED, 6, 1)
				.add(RARITY, 40, 10)
				.add(ATTACK_DAMAGE, 4.5, 0.5)
				.add(ATTACK_SPEED, -2.8, 0.5)
				.run();
	}

	@Test
	void testGoldSword() {
		var sword = Construct.builder()
				.id("forgero:gold-sword")
				.addIngredient(state("forgero:oak-handle"))
				.addIngredient(state("forgero:gold-shortsword_blade"))
				.build();

		AttributeTester.tester(sword)
				.add(DURABILITY, 20, 10)
				.add(MINING_SPEED, 12, 1)
				.add(RARITY, 60, 10)
				.add(ATTACK_DAMAGE, 3, 0.5)
				.add(ATTACK_SPEED, -2.8, 0.5)
				.run();
	}

	@Test
	void testDiamondSword() {
		var sword = Construct.builder()
				.id("forgero:diamond-sword")
				.addIngredient(state("forgero:oak-handle"))
				.addIngredient(state("forgero:diamond-shortsword_blade"))
				.build();

		AttributeTester.tester(sword)
				.add(DURABILITY, 1500, 300)
				.add(MINING_SPEED, 8, 1)
				.add(RARITY, 80, 10)
				.add(ATTACK_DAMAGE, 5.6, 0.5)
				.add(ATTACK_SPEED, -2.8, 0.5)
				.run();
	}

	@Test
	void testNetheriteSword() {
		var sword = Construct.builder()
				.id("forgero:netherite-sword")
				.addIngredient(state("forgero:oak-handle"))
				.addIngredient(state("forgero:netherite-shortsword_blade"))
				.build();

		AttributeTester.tester(sword)
				.add(DURABILITY, 1700, 300)
				.add(MINING_SPEED, 9, 1)
				.add(RARITY, 110, 10)
				.add(ATTACK_DAMAGE, 6, 1.5)
				.add(ATTACK_SPEED, -2.8, 0.5)
				.run();
	}

	@Test
	void testDiamondBroadSword() {
		var sword = Construct.builder()
				.id("forgero:diamond-sword")
				.addIngredient(state("forgero:oak-handle"))
				.addIngredient(state("forgero:diamond-broadsword_blade"))
				.build();

		AttributeTester.tester(sword)
				.add(DURABILITY, 1900, 200)
				.add(MINING_SPEED, 9, 1)
				.add(RARITY, 88, 10)
				.add(ATTACK_DAMAGE, 7, 1.5)
				.add(ATTACK_SPEED, -3.2, 0.2)
				.run();
	}

	@Test
	void testDiamondKatana() {
		var sword = Construct.builder()
				.id("forgero:diamond-sword")
				.addIngredient(state("forgero:oak-handle"))
				.addIngredient(state("forgero:diamond-katana_blade"))
				.build();

		AttributeTester.tester(sword)
				.add(DURABILITY, 1300, 200)
				.add(MINING_SPEED, 9, 1)
				.add(RARITY, 105, 10)
				.add(ATTACK_DAMAGE, 7, 1.5)
				.add(ATTACK_SPEED, -2.7, 0.2)
				.run();
	}

	@Test
	void testDiamondKnife() {
		var sword = Construct.builder()
				.id("forgero:diamond-sword")
				.addIngredient(state("forgero:oak-handle"))
				.addIngredient(state("forgero:diamond-knife_blade"))
				.build();

		AttributeTester.tester(sword)
				.add(DURABILITY, 700, 200)
				.add(MINING_SPEED, 9, 1)
				.add(RARITY, 60, 10)
				.add(ATTACK_DAMAGE, 7, 1.5)
				.add(ATTACK_SPEED, -2.3, 0.2)
				.run();
	}


	@Test
	void testDiamondSaber() {
		var sword = Construct.builder()
				.id("forgero:diamond-sword")
				.addIngredient(state("forgero:oak-handle"))
				.addIngredient(state("forgero:diamond-saber_blade"))
				.build();

		AttributeTester.tester(sword)
				.add(DURABILITY, 1600, 200)
				.add(MINING_SPEED, 9, 1)
				.add(RARITY, 88, 10)
				.add(ATTACK_DAMAGE, 7, 1.5)
				.add(ATTACK_SPEED, -2.6, 0.2)
				.run();
	}
}
