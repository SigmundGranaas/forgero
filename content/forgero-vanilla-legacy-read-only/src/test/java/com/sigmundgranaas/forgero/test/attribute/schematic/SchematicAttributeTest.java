package com.sigmundgranaas.forgero.test.attribute.schematic;

import static com.sigmundgranaas.forgero.test.util.StateHelper.state;
import static org.junit.jupiter.api.Assertions.*;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningSpeed;
import com.sigmundgranaas.forgero.test.util.AttributeTester;
import com.sigmundgranaas.forgero.test.util.ForgeroPackageTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for schematic-based attribute variations.
 * Different tool types (pickaxe, sword, axe, shovel, hoe) have different
 * schematic-provided attributes that should be consistent across materials.
 */
@DisplayName("Schematic Attribute Tests")
public class SchematicAttributeTest extends ForgeroPackageTest {

	@Nested
	@DisplayName("Schematic Attack Speed Patterns")
	class SchematicAttackSpeedPatterns {

		@Test
		@DisplayName("Axes are slower than swords for iron material")
		void testIronAxeSlowerThanSword() {
			var sword = state("forgero:iron-sword");
			var axe = state("forgero:iron-axe");

			float swordSpeed = sword.stream().applyAttribute(AttackSpeed.KEY);
			float axeSpeed = axe.stream().applyAttribute(AttackSpeed.KEY);

			// Axes have -4.5 attack speed, swords have -4
			assertTrue(swordSpeed > axeSpeed,
					"Sword speed (" + swordSpeed + ") should be faster than axe (" + axeSpeed + ")");
		}

		@Test
		@DisplayName("Axes are slower than swords for diamond material")
		void testDiamondAxeSlowerThanSword() {
			var sword = state("forgero:diamond-sword");
			var axe = state("forgero:diamond-axe");

			float swordSpeed = sword.stream().applyAttribute(AttackSpeed.KEY);
			float axeSpeed = axe.stream().applyAttribute(AttackSpeed.KEY);

			assertTrue(swordSpeed > axeSpeed,
					"Diamond sword speed (" + swordSpeed + ") should be faster than axe (" + axeSpeed + ")");
		}

		@Test
		@DisplayName("Pickaxe and sword have similar attack speeds")
		void testPickaxeAndSwordSimilarSpeed() {
			var sword = state("forgero:iron-sword");
			var pickaxe = state("forgero:iron-pickaxe");

			float swordSpeed = sword.stream().applyAttribute(AttackSpeed.KEY);
			float pickaxeSpeed = pickaxe.stream().applyAttribute(AttackSpeed.KEY);

			// Both have -4 from schematic, should be similar
			assertEquals(swordSpeed, pickaxeSpeed, 1.0,
					"Sword and pickaxe should have similar attack speed");
		}

		@Test
		@DisplayName("Shovel has standard attack speed like pickaxe")
		void testShovelStandardSpeed() {
			var shovel = state("forgero:iron-shovel");
			var pickaxe = state("forgero:iron-pickaxe");

			float shovelSpeed = shovel.stream().applyAttribute(AttackSpeed.KEY);
			float pickaxeSpeed = pickaxe.stream().applyAttribute(AttackSpeed.KEY);

			// Both have -4 from schematic
			assertEquals(shovelSpeed, pickaxeSpeed, 1.0,
					"Shovel and pickaxe should have similar attack speed");
		}
	}

	@Nested
	@DisplayName("Schematic Attack Damage Patterns")
	class SchematicAttackDamagePatterns {

		@Test
		@DisplayName("Axes deal more damage than swords")
		void testAxeHigherDamageThanSword() {
			var sword = state("forgero:iron-sword");
			var axe = state("forgero:iron-axe");

			float swordDamage = sword.stream().applyAttribute(AttackDamage.KEY);
			float axeDamage = axe.stream().applyAttribute(AttackDamage.KEY);

			// Axe has +4 from schematic, sword has +2
			assertTrue(axeDamage > swordDamage,
					"Axe damage (" + axeDamage + ") should exceed sword (" + swordDamage + ")");
		}

		@Test
		@DisplayName("Shovels have lower attack damage than swords")
		void testShovelLowerDamageThanSword() {
			var sword = state("forgero:iron-sword");
			var shovel = state("forgero:iron-shovel");

			float swordDamage = sword.stream().applyAttribute(AttackDamage.KEY);
			float shovelDamage = shovel.stream().applyAttribute(AttackDamage.KEY);

			// Sword has +2 from schematic, shovel has +0.5
			assertTrue(swordDamage > shovelDamage,
					"Sword damage (" + swordDamage + ") should exceed shovel (" + shovelDamage + ")");
		}

		@Test
		@DisplayName("Pickaxes have moderate attack damage")
		void testPickaxeModerateDamage() {
			var pickaxe = state("forgero:iron-pickaxe");

			AttributeTester.tester(pickaxe)
					.add(AttackDamage.KEY, 4, 2)  // Material base + schematic contribution
					.run();
		}

		@Test
		@DisplayName("Diamond axe deals significant damage")
		void testDiamondAxeHighDamage() {
			var axe = state("forgero:diamond-axe");

			AttributeTester.tester(axe)
					.add(AttackDamage.KEY, 9, 3)  // Diamond material + axe schematic (+4)
					.run();
		}
	}

	@Nested
	@DisplayName("Tool Type Consistency")
	class ToolTypeConsistency {

		@Test
		@DisplayName("Same material pickaxes have consistent attributes")
		void testPickaxeConsistency() {
			var ironPick = state("forgero:iron-pickaxe");
			var goldPick = state("forgero:gold-pickaxe");

			// Both pickaxes should have same schematic speed
			float ironSpeed = ironPick.stream().applyAttribute(AttackSpeed.KEY);
			float goldSpeed = goldPick.stream().applyAttribute(AttackSpeed.KEY);

			// Schematic speed is consistent (-4), should be similar
			assertEquals(ironSpeed, goldSpeed, 1.0,
					"Pickaxe attack speed should be consistent across materials");
		}

		@Test
		@DisplayName("Same material swords have consistent schematic effects")
		void testSwordConsistency() {
			var ironSword = state("forgero:iron-sword");
			var diamondSword = state("forgero:diamond-sword");

			// Schematic contributes same attack speed modifier
			float ironSpeed = ironSword.stream().applyAttribute(AttackSpeed.KEY);
			float diamondSpeed = diamondSword.stream().applyAttribute(AttackSpeed.KEY);

			assertEquals(ironSpeed, diamondSpeed, 1.0,
					"Sword attack speed should be consistent across materials");
		}

		@Test
		@DisplayName("Same material axes have consistent schematic effects")
		void testAxeConsistency() {
			var ironAxe = state("forgero:iron-axe");
			var diamondAxe = state("forgero:diamond-axe");

			// Axes should both be slow (-4.5 from schematic)
			float ironSpeed = ironAxe.stream().applyAttribute(AttackSpeed.KEY);
			float diamondSpeed = diamondAxe.stream().applyAttribute(AttackSpeed.KEY);

			assertEquals(ironSpeed, diamondSpeed, 1.0,
					"Axe attack speed should be consistent across materials");
		}
	}

	@Nested
	@DisplayName("Material Tier Preserves Schematic Patterns")
	class MaterialTierPreservesSchematic {

		@Test
		@DisplayName("Stone tools follow same speed pattern as iron")
		void testStoneToolSpeedPattern() {
			var stoneSword = state("forgero:cobblestone-sword");
			var stoneAxe = state("forgero:cobblestone-axe");

			float swordSpeed = stoneSword.stream().applyAttribute(AttackSpeed.KEY);
			float axeSpeed = stoneAxe.stream().applyAttribute(AttackSpeed.KEY);

			// Same pattern: sword faster than axe
			assertTrue(swordSpeed > axeSpeed,
					"Stone sword should be faster than stone axe");
		}

		@Test
		@DisplayName("Netherite tools follow same damage pattern")
		void testNetheriteToolDamagePattern() {
			var netheriteSword = state("forgero:netherite-sword");
			var netheriteAxe = state("forgero:netherite-axe");

			float swordDamage = netheriteSword.stream().applyAttribute(AttackDamage.KEY);
			float axeDamage = netheriteAxe.stream().applyAttribute(AttackDamage.KEY);

			// Same pattern: axe hits harder
			assertTrue(axeDamage > swordDamage,
					"Netherite axe (" + axeDamage + ") should hit harder than sword (" + swordDamage + ")");
		}

		@Test
		@DisplayName("Copper tools maintain schematic patterns")
		void testCopperToolPattern() {
			var copperPickaxe = state("forgero:copper-pickaxe");
			var copperShovel = state("forgero:copper-shovel");

			// Both should have similar attack speed (from schematic)
			float pickaxeSpeed = copperPickaxe.stream().applyAttribute(AttackSpeed.KEY);
			float shovelSpeed = copperShovel.stream().applyAttribute(AttackSpeed.KEY);

			assertEquals(pickaxeSpeed, shovelSpeed, 1.0,
					"Copper tools should follow same speed pattern");
		}
	}

	@Nested
	@DisplayName("Schematic Mining Attributes")
	class SchematicMiningAttributes {

		@Test
		@DisplayName("Pickaxe is optimized for mining speed")
		void testPickaxeMiningSpeed() {
			AttributeTester.tester(state("forgero:iron-pickaxe"))
					.add(MiningSpeed.KEY, 6, 2)
					.run();
		}

		@Test
		@DisplayName("Axe is effective for wood")
		void testAxeMiningSpeed() {
			AttributeTester.tester(state("forgero:iron-axe"))
					.add(MiningSpeed.KEY, 6, 2)
					.run();
		}

		@Test
		@DisplayName("Shovel is effective for soft blocks")
		void testShovelMiningSpeed() {
			AttributeTester.tester(state("forgero:iron-shovel"))
					.add(MiningSpeed.KEY, 6, 2)
					.run();
		}

		@Test
		@DisplayName("Hoe has reasonable mining capability")
		void testHoeMiningSpeed() {
			AttributeTester.tester(state("forgero:iron-hoe"))
					.add(MiningSpeed.KEY, 6, 2)
					.run();
		}
	}

	@Nested
	@DisplayName("Cross-Material Tool Type Comparisons")
	class CrossMaterialComparisons {

		@Test
		@DisplayName("All iron tools have positive durability")
		void testAllIronToolsDurability() {
			var pickaxe = state("forgero:iron-pickaxe");
			var sword = state("forgero:iron-sword");
			var axe = state("forgero:iron-axe");
			var shovel = state("forgero:iron-shovel");
			var hoe = state("forgero:iron-hoe");

			// All should have reasonable durability from iron material
			AttributeTester.tester(pickaxe).add(Durability.KEY, 250, 150).run();
			AttributeTester.tester(sword).add(Durability.KEY, 250, 150).run();
			AttributeTester.tester(axe).add(Durability.KEY, 250, 150).run();
			AttributeTester.tester(shovel).add(Durability.KEY, 250, 150).run();
			AttributeTester.tester(hoe).add(Durability.KEY, 250, 150).run();
		}

		@Test
		@DisplayName("All diamond tools have high durability")
		void testAllDiamondToolsDurability() {
			var pickaxe = state("forgero:diamond-pickaxe");
			var sword = state("forgero:diamond-sword");
			var axe = state("forgero:diamond-axe");

			// All should have high durability from diamond material
			AttributeTester.tester(pickaxe).add(Durability.KEY, 1500, 800).run();
			AttributeTester.tester(sword).add(Durability.KEY, 1500, 800).run();
			AttributeTester.tester(axe).add(Durability.KEY, 1500, 800).run();
		}

		@Test
		@DisplayName("Tool type doesn't significantly affect durability")
		void testToolTypeDurabilityConsistency() {
			var ironPick = state("forgero:iron-pickaxe");
			var ironSword = state("forgero:iron-sword");

			float pickDurability = ironPick.stream().applyAttribute(Durability.KEY);
			float swordDurability = ironSword.stream().applyAttribute(Durability.KEY);

			// Same material should give similar base durability
			assertEquals(pickDurability, swordDurability, 100,
					"Same material tools should have similar durability");
		}
	}
}
