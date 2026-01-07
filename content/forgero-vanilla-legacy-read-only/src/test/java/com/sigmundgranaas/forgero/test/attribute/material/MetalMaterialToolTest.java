package com.sigmundgranaas.forgero.test.attribute.material;

import static com.sigmundgranaas.forgero.test.util.StateHelper.state;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningLevel;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Rarity;
import com.sigmundgranaas.forgero.test.util.AttributeTester;
import com.sigmundgranaas.forgero.test.util.ForgeroPackageTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for metal material tools (iron, gold, copper, netherite).
 * Validates that metal tools have attributes matching vanilla Minecraft expectations.
 */
@DisplayName("Metal Material Tool Attributes")
public class MetalMaterialToolTest extends ForgeroPackageTest {

	@Nested
	@DisplayName("Iron Tools")
	class IronTools {

		@Test
		@DisplayName("Iron pickaxe has iron-tier attributes")
		void testIronPickaxe() {
			AttributeTester.tester(state("forgero:iron-pickaxe"))
					.add(Durability.KEY, 250, 50)
					.add(MiningSpeed.KEY, 6, 1)
					.add(Rarity.KEY, 40, 10)
					.add(AttackDamage.KEY, 4, 0.5)
					.add(AttackSpeed.KEY, -2.0, 0.5)
					.add(MiningLevel.KEY, 2, 0.5)
					.run();
		}

		@Test
		@DisplayName("Iron sword has iron-tier combat attributes")
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
		@DisplayName("Iron axe has iron-tier attributes")
		void testIronAxe() {
			AttributeTester.tester(state("forgero:iron-axe"))
					.add(Durability.KEY, 250, 100)
					.add(MiningSpeed.KEY, 6, 2)
					.add(AttackDamage.KEY, 9, 2)
					.add(AttackSpeed.KEY, -3.0, 1)
					.run();
		}

		@Test
		@DisplayName("Iron shovel has iron-tier attributes")
		void testIronShovel() {
			AttributeTester.tester(state("forgero:iron-shovel"))
					.add(Durability.KEY, 250, 50)
					.add(MiningSpeed.KEY, 6, 1)
					.add(AttackDamage.KEY, 4.5, 0.5)
					.run();
		}

		@Test
		@DisplayName("Iron hoe has iron-tier attributes")
		void testIronHoe() {
			AttributeTester.tester(state("forgero:iron-hoe"))
					.add(Durability.KEY, 250, 50)
					.add(AttackSpeed.KEY, -1.0, 1.0)
					.run();
		}
	}

	@Nested
	@DisplayName("Gold Tools")
	class GoldTools {

		@Test
		@DisplayName("Gold pickaxe is fragile but fast")
		void testGoldPickaxe() {
			AttributeTester.tester(state("forgero:gold-pickaxe"))
					.add(Durability.KEY, 41, 15)
					.add(MiningSpeed.KEY, 12, 1)
					.add(Rarity.KEY, 60, 10)
					.add(AttackDamage.KEY, 2, 0.5)
					.add(MiningLevel.KEY, 1, 0.5)
					.run();
		}

		@Test
		@DisplayName("Gold sword is fragile but enchantable")
		void testGoldSword() {
			AttributeTester.tester(state("forgero:gold-sword"))
					.add(Durability.KEY, 41, 15)
					.add(MiningSpeed.KEY, 12, 1)
					.add(Rarity.KEY, 60, 10)
					.add(AttackDamage.KEY, 4, 0.5)
					.run();
		}

		@Test
		@DisplayName("Gold axe is fragile but fast")
		void testGoldAxe() {
			AttributeTester.tester(state("forgero:gold-axe"))
					.add(Durability.KEY, 41, 15)
					.add(MiningSpeed.KEY, 12, 1)
					.add(AttackDamage.KEY, 7, 1)
					.run();
		}
	}

	@Nested
	@DisplayName("Copper Tools")
	class CopperTools {

		@Test
		@DisplayName("Copper pickaxe has mid-tier attributes")
		void testCopperPickaxe() {
			AttributeTester.tester(state("forgero:copper-pickaxe"))
					.add(Durability.KEY, 320, 100)
					.add(MiningSpeed.KEY, 6.5, 2)
					.add(Rarity.KEY, 30, 15)
					.add(AttackDamage.KEY, 3.5, 1)
					.run();
		}

		@Test
		@DisplayName("Copper sword has mid-tier combat attributes")
		void testCopperSword() {
			AttributeTester.tester(state("forgero:copper-sword"))
					.add(Durability.KEY, 320, 100)
					.add(AttackDamage.KEY, 5.5, 1)
					.run();
		}
	}

	@Nested
	@DisplayName("Netherite Tools")
	class NetheriteTools {

		@Test
		@DisplayName("Netherite pickaxe has top-tier attributes")
		void testNetheritePickaxe() {
			AttributeTester.tester(state("forgero:netherite-pickaxe"))
					.add(Durability.KEY, 2100, 300)
					.add(MiningSpeed.KEY, 9, 1)
					.add(Rarity.KEY, 110, 15)
					.add(AttackDamage.KEY, 6, 1.5)
					.add(MiningLevel.KEY, 4, 0.5)
					.run();
		}

		@Test
		@DisplayName("Netherite sword has top-tier combat attributes")
		void testNetheriteSword() {
			AttributeTester.tester(state("forgero:netherite-sword"))
					.add(Durability.KEY, 2100, 300)
					.add(MiningSpeed.KEY, 9, 1)
					.add(Rarity.KEY, 110, 15)
					.add(AttackDamage.KEY, 8, 1.5)
					.run();
		}

		@Test
		@DisplayName("Netherite axe has top-tier attributes")
		void testNetheriteAxe() {
			AttributeTester.tester(state("forgero:netherite-axe"))
					.add(Durability.KEY, 2100, 300)
					.add(MiningSpeed.KEY, 9, 1)
					.add(AttackDamage.KEY, 10, 1.5)
					.run();
		}

		@Test
		@DisplayName("Netherite shovel has top-tier attributes")
		void testNetheriteShovel() {
			AttributeTester.tester(state("forgero:netherite-shovel"))
					.add(Durability.KEY, 2100, 300)
					.add(MiningSpeed.KEY, 9, 1)
					.add(AttackDamage.KEY, 6.5, 1)
					.run();
		}

		@Test
		@DisplayName("Netherite hoe has top-tier attributes")
		void testNetheriteHoe() {
			AttributeTester.tester(state("forgero:netherite-hoe"))
					.add(Durability.KEY, 2100, 300)
					.run();
		}
	}
}
