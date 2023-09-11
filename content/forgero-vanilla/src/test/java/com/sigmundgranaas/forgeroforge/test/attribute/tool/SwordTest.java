package com.sigmundgranaas.forgeroforge.test.attribute.tool;

import static com.sigmundgranaas.forgero.core.property.AttributeType.*;
import static com.sigmundgranaas.forgeroforge.test.util.StateHelper.state;

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
				.add(DURABILITY, 50, 10)
				.add(MINING_SPEED, 2, 1)
				.add(RARITY, 20, 5)
				.add(ATTACK_DAMAGE, 3.5, 0.5)
				.add(ATTACK_SPEED, -2.1, 0.5)
				.run();
	}

	@Test
	void testIronSword() {
		AttributeTester.tester(state("forgero:iron-sword"))
				.add(DURABILITY, 250, 50)
				.add(MINING_SPEED, 6, 1)
				.add(RARITY, 40, 10)
				.add(ATTACK_DAMAGE, 6, 0.5)
				.add(ATTACK_SPEED, -2.1, 0.5)
				.run();
	}

	@Test
	void testGoldSword() {
		AttributeTester.tester(state("forgero:gold-sword"))
				.add(DURABILITY, 41, 10)
				.add(MINING_SPEED, 12, 1)
				.add(RARITY, 60, 10)
				.add(ATTACK_DAMAGE, 4, 0.5)
				.add(ATTACK_SPEED, -2.1, 0.5)
				.run();
	}

	@Test
	void testDiamondSword() {
		AttributeTester.tester(state("forgero:diamond-sword"))
				.add(DURABILITY, 1500, 300)
				.add(MINING_SPEED, 8, 1)
				.add(RARITY, 80, 10)
				.add(ATTACK_DAMAGE, 7, 0.5)
				.add(ATTACK_SPEED, -2.1, 0.5)
				.run();
	}

	@Test
	void testNetheriteSword() {
		AttributeTester.tester(state("forgero:netherite-sword"))
				.add(DURABILITY, 2100, 300)
				.add(MINING_SPEED, 9, 1)
				.add(RARITY, 110, 10)
				.add(ATTACK_DAMAGE, 8, 1.5)
				.add(ATTACK_SPEED, -2.1, 0.5)
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
				.add(DURABILITY, 1600, 200)
				.add(MINING_SPEED, 9, 1)
				.add(RARITY, 100, 20)
				.add(ATTACK_DAMAGE, 7, 1.5)
				.add(ATTACK_SPEED, -2.1, 0.3)
				.run();
	}
}
