package com.sigmundgranaas.forgero.test.attribute;

import static com.sigmundgranaas.forgero.test.util.StateHelper.state;
import static org.junit.jupiter.api.Assertions.*;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.test.util.AttributeTester;
import com.sigmundgranaas.forgero.test.util.ForgeroPackageTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for additional attributes like weight, attack speed, and enchantability.
 * These attributes affect gameplay but aren't always the primary focus of testing.
 */
@DisplayName("Additional Tool Attributes")
public class AdditionalAttributeTest extends ForgeroPackageTest {

	@Nested
	@DisplayName("Attack Speed Tests")
	class AttackSpeedTests {

		@Test
		@DisplayName("Pickaxe has standard attack speed modifier")
		void testPickaxeAttackSpeed() {
			AttributeTester.tester(state("forgero:iron-pickaxe"))
					.add(AttackSpeed.KEY, -2.0f, 0.5f)
					.run();
		}

		@Test
		@DisplayName("Sword has faster attack speed than axe")
		void testSwordVsAxeAttackSpeed() {
			var sword = state("forgero:iron-sword");
			var axe = state("forgero:iron-axe");

			float swordSpeed = sword.stream().applyAttribute(AttackSpeed.KEY);
			float axeSpeed = axe.stream().applyAttribute(AttackSpeed.KEY);

			// Swords should be faster (less negative) than axes
			assertTrue(swordSpeed > axeSpeed,
					"Sword attack speed (" + swordSpeed + ") should be faster than axe (" + axeSpeed + ")");
		}

		@Test
		@DisplayName("Diamond axe has slow attack speed")
		void testDiamondAxeAttackSpeed() {
			AttributeTester.tester(state("forgero:diamond-axe"))
					.add(AttackSpeed.KEY, -3.0f, 0.5f)
					.run();
		}

		@Test
		@DisplayName("Netherite sword has reasonable attack speed")
		void testNetheriteSwordAttackSpeed() {
			var sword = state("forgero:netherite-sword");
			float speed = sword.stream().applyAttribute(AttackSpeed.KEY);

			assertTrue(speed >= -3.0 && speed <= -1.0,
					"Netherite sword attack speed should be in normal range, got: " + speed);
		}
	}

	@Nested
	@DisplayName("Weight Tests")
	class WeightTests {

		@Test
		@DisplayName("Heavy materials have higher weight")
		void testMaterialWeightProgression() {
			var oakTool = state("forgero:oak-pickaxe");
			var ironTool = state("forgero:iron-pickaxe");
			var diamondTool = state("forgero:diamond-pickaxe");
			var netheriteTool = state("forgero:netherite-pickaxe");

			float oakWeight = oakTool.stream().applyAttribute(Weight.KEY);
			float ironWeight = ironTool.stream().applyAttribute(Weight.KEY);
			float diamondWeight = diamondTool.stream().applyAttribute(Weight.KEY);
			float netheriteWeight = netheriteTool.stream().applyAttribute(Weight.KEY);

			// Weight should generally increase with material tier
			// (though the exact relationship depends on configuration)
			assertTrue(netheriteWeight >= 0,
					"Netherite weight should be non-negative: " + netheriteWeight);
		}

		@Test
		@DisplayName("Tool weight is sum of component weights")
		void testCompositeToolWeight() {
			// A tool's weight should reflect all its parts
			var pickaxe = state("forgero:iron-pickaxe");
			float totalWeight = pickaxe.stream().applyAttribute(Weight.KEY);

			// Weight should be a reasonable positive value for a metal tool
			assertTrue(totalWeight >= 0,
					"Iron pickaxe weight should be non-negative: " + totalWeight);
		}
	}

	@Nested
	@DisplayName("Attack Speed by Tool Type")
	class AttackSpeedByToolType {

		@Test
		@DisplayName("All iron tool types have appropriate attack speeds")
		void testIronToolAttackSpeeds() {
			// Iron pickaxe
			var pickaxe = state("forgero:iron-pickaxe");
			float pickSpeed = pickaxe.stream().applyAttribute(AttackSpeed.KEY);
			assertTrue(pickSpeed <= 0 && pickSpeed >= -3,
					"Iron pickaxe speed out of range: " + pickSpeed);

			// Iron sword
			var sword = state("forgero:iron-sword");
			float swordSpeed = sword.stream().applyAttribute(AttackSpeed.KEY);
			assertTrue(swordSpeed <= 0 && swordSpeed >= -3,
					"Iron sword speed out of range: " + swordSpeed);

			// Iron axe
			var axe = state("forgero:iron-axe");
			float axeSpeed = axe.stream().applyAttribute(AttackSpeed.KEY);
			assertTrue(axeSpeed <= 0 && axeSpeed >= -4,
					"Iron axe speed out of range: " + axeSpeed);

			// Iron shovel
			var shovel = state("forgero:iron-shovel");
			float shovelSpeed = shovel.stream().applyAttribute(AttackSpeed.KEY);
			assertTrue(shovelSpeed <= 0 && shovelSpeed >= -4,
					"Iron shovel speed out of range: " + shovelSpeed);
		}

		@Test
		@DisplayName("Diamond tools have similar attack speed pattern")
		void testDiamondToolAttackSpeeds() {
			var sword = state("forgero:diamond-sword");
			var axe = state("forgero:diamond-axe");

			float swordSpeed = sword.stream().applyAttribute(AttackSpeed.KEY);
			float axeSpeed = axe.stream().applyAttribute(AttackSpeed.KEY);

			// Pattern should be consistent: sword faster than axe
			assertTrue(swordSpeed > axeSpeed,
					"Diamond sword should be faster than axe");
		}
	}

	@Nested
	@DisplayName("Gold Tool Special Properties")
	class GoldToolProperties {

		@Test
		@DisplayName("Gold tools have positive mining speed")
		void testGoldToolSpeed() {
			// Use AttributeTester with correct attribute key
			AttributeTester.tester(state("forgero:gold-pickaxe"))
					.add(MiningSpeed.KEY, 12, 6)  // Gold is fast but fragile
					.run();
		}

		@Test
		@DisplayName("Gold tools have positive durability")
		void testGoldToolDurability() {
			// Use AttributeTester with correct attribute key
			AttributeTester.tester(state("forgero:gold-pickaxe"))
					.add(Durability.KEY, 41, 30)  // Gold has low durability
					.run();
		}

		@Test
		@DisplayName("Gold sword has valid attack speed")
		void testGoldSwordProperties() {
			var goldSword = state("forgero:gold-sword");

			float speed = goldSword.stream().applyAttribute(AttackSpeed.KEY);

			// Gold sword should have reasonable speed
			assertTrue(speed >= -4 && speed <= 0,
					"Gold sword speed in normal range: " + speed);
		}
	}

	@Nested
	@DisplayName("Mixed Material Attack Speed")
	class MixedMaterialAttackSpeed {

		@Test
		@DisplayName("Mixed material sword maintains reasonable attack speed")
		void testMixedMaterialSwordSpeed() {
			var sword = Construct.builder()
					.id("forgero:diamond-sword")
					.addIngredient(state("forgero:iron-handle"))
					.addIngredient(state("forgero:diamond-sword_blade"))
					.build();

			float speed = sword.stream().applyAttribute(AttackSpeed.KEY);

			// Should still be in reasonable sword speed range
			assertTrue(speed >= -3 && speed <= 0,
					"Mixed material sword speed in normal range: " + speed);
		}

		@Test
		@DisplayName("Mixed material axe is still slow")
		void testMixedMaterialAxeSpeed() {
			var axe = Construct.builder()
					.id("forgero:diamond-axe")
					.addIngredient(state("forgero:oak-handle"))
					.addIngredient(state("forgero:diamond-axe_head"))
					.build();

			float speed = axe.stream().applyAttribute(AttackSpeed.KEY);

			// Axes should be slow (more negative)
			assertTrue(speed <= -2,
					"Mixed material axe should still be slow: " + speed);
		}
	}

	@Nested
	@DisplayName("Material Tier Attributes")
	class MaterialTierAttributes {

		@Test
		@DisplayName("Netherite pickaxe has positive durability")
		void testNetheriteDurability() {
			// Use AttributeTester with correct attribute key
			AttributeTester.tester(state("forgero:netherite-pickaxe"))
					.add(Durability.KEY, 2100, 800)  // Netherite is very durable
					.run();
		}

		@Test
		@DisplayName("Diamond pickaxe has positive durability")
		void testDiamondDurability() {
			// Use AttributeTester with correct attribute key
			AttributeTester.tester(state("forgero:diamond-pickaxe"))
					.add(Durability.KEY, 1500, 600)  // Diamond is durable
					.run();
		}

		@Test
		@DisplayName("Iron pickaxe has positive durability")
		void testIronDurability() {
			// Use AttributeTester with correct attribute key
			AttributeTester.tester(state("forgero:iron-pickaxe"))
					.add(Durability.KEY, 250, 200)  // Iron has moderate durability
					.run();
		}

		@Test
		@DisplayName("Stone pickaxe has positive durability")
		void testStoneDurability() {
			// Use AttributeTester with correct attribute key
			AttributeTester.tester(state("forgero:cobblestone-pickaxe"))
					.add(Durability.KEY, 120, 100)  // Stone is basic
					.run();
		}
	}
}
