package com.sigmundgranaas.forgero.test.attribute.edge;

import static com.sigmundgranaas.forgero.test.util.StateHelper.state;
import static org.junit.jupiter.api.Assertions.*;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Armor;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningLevel;
import com.sigmundgranaas.forgero.test.util.ForgeroPackageTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Edge case tests for attribute validation.
 * Validates that attributes don't leak between item types and stay within bounds.
 */
@DisplayName("Negative and Edge Case Attribute Tests")
public class NegativeAttributeTest extends ForgeroPackageTest {

	@Nested
	@DisplayName("Armor Should Not Leak to Tools")
	class ArmorLeakTests {

		@Test
		@DisplayName("Iron pickaxe has zero armor")
		void testIronPickaxeNoArmor() {
			var pickaxe = state("forgero:iron-pickaxe");
			float armor = pickaxe.stream().applyAttribute(Armor.KEY);
			assertEquals(0, armor, 0.1, "Iron pickaxe should have 0 armor");
		}

		@Test
		@DisplayName("Diamond sword has zero armor")
		void testDiamondSwordNoArmor() {
			var sword = state("forgero:diamond-sword");
			float armor = sword.stream().applyAttribute(Armor.KEY);
			assertEquals(0, armor, 0.1, "Diamond sword should have 0 armor");
		}

		@Test
		@DisplayName("Netherite axe has zero armor")
		void testNetheriteAxeNoArmor() {
			var axe = state("forgero:netherite-axe");
			float armor = axe.stream().applyAttribute(Armor.KEY);
			assertEquals(0, armor, 0.1, "Netherite axe should have 0 armor");
		}
	}

	@Nested
	@DisplayName("Durability Must Be Positive")
	class DurabilityBoundsTests {

		@Test
		@DisplayName("Oak pickaxe durability is positive")
		void testOakPickaxeDurabilityPositive() {
			var pickaxe = state("forgero:oak-pickaxe");
			float durability = pickaxe.stream().applyAttribute(Durability.KEY);
			assertTrue(durability > 0, "Durability must be positive, got: " + durability);
		}

		@Test
		@DisplayName("Gold pickaxe durability is positive despite being fragile")
		void testGoldPickaxeDurabilityPositive() {
			var pickaxe = state("forgero:gold-pickaxe");
			float durability = pickaxe.stream().applyAttribute(Durability.KEY);
			assertTrue(durability > 0, "Gold durability must be positive, got: " + durability);
		}
	}

	@Nested
	@DisplayName("Attack Damage Must Be Non-Negative")
	class AttackDamageBoundsTests {

		@Test
		@DisplayName("All pickaxes have non-negative attack damage")
		void testPickaxeAttackDamageNonNegative() {
			String[] pickaxes = {
					"forgero:oak-pickaxe",
					"forgero:iron-pickaxe",
					"forgero:gold-pickaxe",
					"forgero:diamond-pickaxe",
					"forgero:netherite-pickaxe"
			};

			for (String id : pickaxes) {
				var pickaxe = state(id);
				float damage = pickaxe.stream().applyAttribute(AttackDamage.KEY);
				assertTrue(damage >= 0, id + " attack damage must be non-negative, got: " + damage);
			}
		}
	}

	@Nested
	@DisplayName("Mining Level Must Be Valid")
	class MiningLevelBoundsTests {

		@Test
		@DisplayName("Mining level is within valid range (0-4)")
		void testMiningLevelRange() {
			String[] tools = {
					"forgero:oak-pickaxe",
					"forgero:cobblestone-pickaxe",
					"forgero:iron-pickaxe",
					"forgero:diamond-pickaxe",
					"forgero:netherite-pickaxe"
			};

			for (String id : tools) {
				var tool = state(id);
				float miningLevel = tool.stream().applyAttribute(MiningLevel.KEY);
				assertTrue(miningLevel >= 0 && miningLevel <= 4,
						id + " mining level must be 0-4, got: " + miningLevel);
			}
		}
	}

	@Nested
	@DisplayName("Tier Ordering")
	class TierOrderingTests {

		@Test
		@DisplayName("Higher tier materials have higher durability")
		void testDurabilityTierOrdering() {
			float woodDur = state("forgero:oak-pickaxe").stream().applyAttribute(Durability.KEY);
			float stoneDur = state("forgero:cobblestone-pickaxe").stream().applyAttribute(Durability.KEY);
			float ironDur = state("forgero:iron-pickaxe").stream().applyAttribute(Durability.KEY);
			float diamondDur = state("forgero:diamond-pickaxe").stream().applyAttribute(Durability.KEY);
			float netheriteDur = state("forgero:netherite-pickaxe").stream().applyAttribute(Durability.KEY);

			assertTrue(woodDur < stoneDur, "Wood < Stone durability");
			assertTrue(stoneDur < ironDur, "Stone < Iron durability");
			assertTrue(ironDur < diamondDur, "Iron < Diamond durability");
			assertTrue(diamondDur < netheriteDur, "Diamond < Netherite durability");
		}

		@Test
		@DisplayName("Higher tier materials have higher mining level")
		void testMiningLevelTierOrdering() {
			float woodLevel = state("forgero:oak-pickaxe").stream().applyAttribute(MiningLevel.KEY);
			float stoneLevel = state("forgero:cobblestone-pickaxe").stream().applyAttribute(MiningLevel.KEY);
			float ironLevel = state("forgero:iron-pickaxe").stream().applyAttribute(MiningLevel.KEY);
			float diamondLevel = state("forgero:diamond-pickaxe").stream().applyAttribute(MiningLevel.KEY);
			float netheriteLevel = state("forgero:netherite-pickaxe").stream().applyAttribute(MiningLevel.KEY);

			assertTrue(woodLevel < stoneLevel, "Wood < Stone mining level");
			assertTrue(stoneLevel < ironLevel, "Stone < Iron mining level");
			assertTrue(ironLevel < diamondLevel, "Iron < Diamond mining level");
			assertTrue(diamondLevel < netheriteLevel, "Diamond < Netherite mining level");
		}
	}
}
