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
 * Comprehensive tests for mineral material tools (diamond, emerald, amethyst).
 * Mineral tools are high tier with excellent durability and mining level 3.
 */
@DisplayName("Mineral Material Tool Attributes")
public class MineralMaterialToolTest extends ForgeroPackageTest {

	@Nested
	@DisplayName("Diamond Tools")
	class DiamondTools {

		@Test
		@DisplayName("Diamond pickaxe has diamond-tier attributes")
		void testDiamondPickaxe() {
			AttributeTester.tester(state("forgero:diamond-pickaxe"))
					.add(Durability.KEY, 1500, 300)
					.add(MiningSpeed.KEY, 8, 1)
					.add(Rarity.KEY, 80, 15)
					.add(AttackDamage.KEY, 5, 0.5)
					.add(MiningLevel.KEY, 3, 0.5)
					.run();
		}

		@Test
		@DisplayName("Diamond sword has diamond-tier combat attributes")
		void testDiamondSword() {
			AttributeTester.tester(state("forgero:diamond-sword"))
					.add(Durability.KEY, 1500, 300)
					.add(MiningSpeed.KEY, 8, 1)
					.add(Rarity.KEY, 80, 15)
					.add(AttackDamage.KEY, 7, 0.5)
					.run();
		}

		@Test
		@DisplayName("Diamond axe has diamond-tier attributes")
		void testDiamondAxe() {
			AttributeTester.tester(state("forgero:diamond-axe"))
					.add(Durability.KEY, 1500, 300)
					.add(MiningSpeed.KEY, 8, 1)
					.add(AttackDamage.KEY, 9, 1)
					.run();
		}

		@Test
		@DisplayName("Diamond shovel has diamond-tier attributes")
		void testDiamondShovel() {
			AttributeTester.tester(state("forgero:diamond-shovel"))
					.add(Durability.KEY, 1500, 300)
					.add(MiningSpeed.KEY, 8, 1)
					.add(AttackDamage.KEY, 5.5, 0.5)
					.run();
		}

		@Test
		@DisplayName("Diamond hoe has diamond-tier attributes")
		void testDiamondHoe() {
			AttributeTester.tester(state("forgero:diamond-hoe"))
					.add(Durability.KEY, 1500, 300)
					.run();
		}
	}

	@Nested
	@DisplayName("Emerald Tools")
	class EmeraldTools {

		@Test
		@DisplayName("Emerald pickaxe has mineral-tier attributes")
		void testEmeraldPickaxe() {
			AttributeTester.tester(state("forgero:emerald-pickaxe"))
					.add(Durability.KEY, 1550, 500)
					.add(MiningSpeed.KEY, 8, 2)
					.add(Rarity.KEY, 80, 20)
					.add(MiningLevel.KEY, 3, 0.5)
					.run();
		}

		@Test
		@DisplayName("Emerald sword has mineral-tier combat attributes")
		void testEmeraldSword() {
			AttributeTester.tester(state("forgero:emerald-sword"))
					.add(Durability.KEY, 1550, 500)
					.add(AttackDamage.KEY, 7, 1)
					.run();
		}
	}

	@Nested
	@DisplayName("Amethyst Tools")
	class AmethystTools {

		@Test
		@DisplayName("Amethyst pickaxe has mineral-tier attributes")
		void testAmethystPickaxe() {
			AttributeTester.tester(state("forgero:amethyst-pickaxe"))
					.add(Durability.KEY, 1550, 500)
					.add(MiningSpeed.KEY, 8, 2)
					.add(Rarity.KEY, 80, 20)
					.run();
		}

		@Test
		@DisplayName("Amethyst sword has mineral-tier combat attributes")
		void testAmethystSword() {
			AttributeTester.tester(state("forgero:amethyst-sword"))
					.add(Durability.KEY, 1550, 500)
					.add(AttackDamage.KEY, 7, 1)
					.run();
		}
	}
}
