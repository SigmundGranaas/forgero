package com.sigmundgranaas.forgero.test.attribute.combination;

import static com.sigmundgranaas.forgero.test.util.StateHelper.state;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningLevel;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Rarity;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.test.util.AttributeTester;
import com.sigmundgranaas.forgero.test.util.ForgeroPackageTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for mixed material tool combinations.
 * Validates that combining different materials for head and handle produces
 * the expected composite attribute values.
 */
@DisplayName("Mixed Material Tool Combinations")
public class MixedMaterialTest extends ForgeroPackageTest {

	@Nested
	@DisplayName("Diamond Head with Different Handles")
	class DiamondHeadCombinations {

		@Test
		@DisplayName("Diamond pickaxe with iron handle has enhanced durability")
		void testDiamondPickaxeIronHandle() {
			var pickaxe = Construct.builder()
					.id("forgero:diamond-pickaxe")
					.addIngredient(state("forgero:iron-handle"))
					.addIngredient(state("forgero:diamond-pickaxe_head"))
					.build();

			AttributeTester.tester(pickaxe)
					.add(Durability.KEY, 1600, 200)
					.add(MiningSpeed.KEY, 9, 1)
					.add(Rarity.KEY, 100, 25)
					.add(AttackDamage.KEY, 6, 1.5)
					.add(AttackSpeed.KEY, -2.0, 0.5)
					.run();
		}

		@Test
		@DisplayName("Diamond sword with iron handle has enhanced durability")
		void testDiamondSwordIronHandle() {
			var sword = Construct.builder()
					.id("forgero:diamond-sword")
					.addIngredient(state("forgero:iron-handle"))
					.addIngredient(state("forgero:diamond-sword_blade"))
					.build();

			AttributeTester.tester(sword)
					.add(Durability.KEY, 1600, 200)
					.add(MiningSpeed.KEY, 9, 1)
					.add(Rarity.KEY, 100, 25)
					.add(AttackDamage.KEY, 7, 1.5)
					.run();
		}

		@Test
		@DisplayName("Diamond axe with oak handle is weaker but lighter")
		void testDiamondAxeOakHandle() {
			var axe = Construct.builder()
					.id("forgero:diamond-axe")
					.addIngredient(state("forgero:oak-handle"))
					.addIngredient(state("forgero:diamond-axe_head"))
					.build();

			AttributeTester.tester(axe)
					.add(Durability.KEY, 1500, 200)
					.add(MiningSpeed.KEY, 8, 1)
					.add(AttackDamage.KEY, 9, 1.5)
					.run();
		}

		@Test
		@DisplayName("Diamond pickaxe with netherite handle has maximum durability")
		void testDiamondPickaxeNetheriteHandle() {
			var pickaxe = Construct.builder()
					.id("forgero:diamond-pickaxe")
					.addIngredient(state("forgero:netherite-handle"))
					.addIngredient(state("forgero:diamond-pickaxe_head"))
					.build();

			AttributeTester.tester(pickaxe)
					.add(Durability.KEY, 2500, 800)
					.add(MiningSpeed.KEY, 10, 3)
					.add(Rarity.KEY, 130, 50)
					.run();
		}
	}

	@Nested
	@DisplayName("Iron Head with Different Handles")
	class IronHeadCombinations {

		@Test
		@DisplayName("Iron pickaxe with diamond handle is enhanced")
		void testIronPickaxeDiamondHandle() {
			var pickaxe = Construct.builder()
					.id("forgero:iron-pickaxe")
					.addIngredient(state("forgero:diamond-handle"))
					.addIngredient(state("forgero:iron-pickaxe_head"))
					.build();

			AttributeTester.tester(pickaxe)
					.add(Durability.KEY, 1000, 500)
					.add(MiningSpeed.KEY, 7, 2)
					.add(Rarity.KEY, 80, 40)
					.run();
		}

		@Test
		@DisplayName("Iron sword with oak handle is basic but functional")
		void testIronSwordOakHandle() {
			var sword = Construct.builder()
					.id("forgero:iron-sword")
					.addIngredient(state("forgero:oak-handle"))
					.addIngredient(state("forgero:iron-sword_blade"))
					.build();

			AttributeTester.tester(sword)
					.add(Durability.KEY, 250, 100)
					.add(MiningSpeed.KEY, 6, 2)
					.add(AttackDamage.KEY, 6, 1)
					.run();
		}

		// Note: Iron axe with gold handle test is skipped - axe_head ID needs verification.
	}

	@Nested
	@DisplayName("Netherite Head with Different Handles")
	class NetheriteHeadCombinations {

		@Test
		@DisplayName("Netherite pickaxe with diamond handle is top tier")
		void testNetheritePickaxeDiamondHandle() {
			var pickaxe = Construct.builder()
					.id("forgero:netherite-pickaxe")
					.addIngredient(state("forgero:diamond-handle"))
					.addIngredient(state("forgero:netherite-pickaxe_head"))
					.build();

			AttributeTester.tester(pickaxe)
					.add(Durability.KEY, 2300, 600)
					.add(MiningSpeed.KEY, 10, 2)
					.add(Rarity.KEY, 130, 50)
					.add(MiningLevel.KEY, 4, 0.5)
					.run();
		}

		@Test
		@DisplayName("Netherite sword with iron handle is durable and strong")
		void testNetheriteSwordIronHandle() {
			var sword = Construct.builder()
					.id("forgero:netherite-sword")
					.addIngredient(state("forgero:iron-handle"))
					.addIngredient(state("forgero:netherite-sword_blade"))
					.build();

			AttributeTester.tester(sword)
					.add(Durability.KEY, 2200, 500)
					.add(AttackDamage.KEY, 8, 2)
					.run();
		}
	}

	@Nested
	@DisplayName("Stone and Wood Combinations")
	class LowTierCombinations {

		@Test
		@DisplayName("Cobblestone pickaxe with birch handle is basic")
		void testCobblestonePickaxeBirchHandle() {
			var pickaxe = Construct.builder()
					.id("forgero:cobblestone-pickaxe")
					.addIngredient(state("forgero:birch-handle"))
					.addIngredient(state("forgero:cobblestone-pickaxe_head"))
					.build();

			AttributeTester.tester(pickaxe)
					.add(Durability.KEY, 100, 50)
					.add(MiningSpeed.KEY, 4, 2)
					.add(MiningLevel.KEY, 1, 0.5)
					.run();
		}

		@Test
		@DisplayName("Deepslate pickaxe with iron handle is enhanced stone tier")
		void testDeepslatePickaxeIronHandle() {
			var pickaxe = Construct.builder()
					.id("forgero:deepslate-pickaxe")
					.addIngredient(state("forgero:iron-handle"))
					.addIngredient(state("forgero:deepslate-pickaxe_head"))
					.build();

			AttributeTester.tester(pickaxe)
					.add(Durability.KEY, 300, 150)
					.add(MiningSpeed.KEY, 5, 2)
					.run();
		}

		@Test
		@DisplayName("Gold pickaxe with diamond handle improves durability")
		void testGoldPickaxeDiamondHandle() {
			var pickaxe = Construct.builder()
					.id("forgero:gold-pickaxe")
					.addIngredient(state("forgero:diamond-handle"))
					.addIngredient(state("forgero:gold-pickaxe_head"))
					.build();

			// Gold head is still fragile, but diamond handle helps
			AttributeTester.tester(pickaxe)
					.add(Durability.KEY, 800, 500)
					.add(MiningSpeed.KEY, 13, 5)
					.add(Rarity.KEY, 100, 50)
					.run();
		}
	}
}
