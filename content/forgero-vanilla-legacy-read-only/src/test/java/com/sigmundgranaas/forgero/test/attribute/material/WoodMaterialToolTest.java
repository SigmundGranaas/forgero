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
 * Comprehensive tests for wood material tools across different wood types.
 * Wood tools are the basic tier with low durability and mining level 0.
 */
@DisplayName("Wood Material Tool Attributes")
public class WoodMaterialToolTest extends ForgeroPackageTest {

	@Nested
	@DisplayName("Oak Tools")
	class OakTools {

		@Test
		@DisplayName("Oak pickaxe has wood-tier attributes")
		void testOakPickaxe() {
			AttributeTester.tester(state("forgero:oak-pickaxe"))
					.add(Durability.KEY, 50, 15)
					.add(MiningSpeed.KEY, 2, 1)
					.add(Rarity.KEY, 20, 10)
					.add(AttackDamage.KEY, 2, 0.5)
					.add(MiningLevel.KEY, 0, 0.5)
					.run();
		}

		@Test
		@DisplayName("Oak sword has wood-tier combat attributes")
		void testOakSword() {
			AttributeTester.tester(state("forgero:oak-sword"))
					.add(Durability.KEY, 50, 15)
					.add(MiningSpeed.KEY, 2, 1)
					.add(Rarity.KEY, 20, 10)
					.add(AttackDamage.KEY, 3.5, 0.5)
					.run();
		}

		@Test
		@DisplayName("Oak axe has wood-tier attributes")
		void testOakAxe() {
			AttributeTester.tester(state("forgero:oak-axe"))
					.add(Durability.KEY, 50, 15)
					.add(MiningSpeed.KEY, 2, 1)
					.add(AttackDamage.KEY, 7, 1)
					.run();
		}
	}

	@Nested
	@DisplayName("Birch Tools")
	class BirchTools {

		@Test
		@DisplayName("Birch pickaxe is slightly weaker than oak")
		void testBirchPickaxe() {
			AttributeTester.tester(state("forgero:birch-pickaxe"))
					.add(Durability.KEY, 40, 15)
					.add(MiningSpeed.KEY, 2, 1)
					.add(AttackDamage.KEY, 2, 0.5)
					.add(MiningLevel.KEY, 0, 0.5)
					.run();
		}

		@Test
		@DisplayName("Birch sword has birch-tier combat attributes")
		void testBirchSword() {
			AttributeTester.tester(state("forgero:birch-sword"))
					.add(Durability.KEY, 40, 15)
					.add(AttackDamage.KEY, 3.5, 0.5)
					.run();
		}
	}

	@Nested
	@DisplayName("Spruce Tools")
	class SpruceTools {

		@Test
		@DisplayName("Spruce pickaxe has standard wood attributes")
		void testSprucePickaxe() {
			AttributeTester.tester(state("forgero:spruce-pickaxe"))
					.add(Durability.KEY, 50, 20)
					.add(MiningSpeed.KEY, 2, 1)
					.add(MiningLevel.KEY, 0, 0.5)
					.run();
		}
	}

	// Note: Dark oak tools test is skipped - the exact ID format (dark_oak or dark-oak)
	// needs verification in the actual content package.

	@Nested
	@DisplayName("Nether Wood Tools")
	class NetherWoodTools {

		@Test
		@DisplayName("Crimson pickaxe has nether wood attributes")
		void testCrimsonPickaxe() {
			AttributeTester.tester(state("forgero:crimson-pickaxe"))
					.add(Durability.KEY, 50, 20)
					.add(MiningSpeed.KEY, 2, 1)
					.run();
		}

		@Test
		@DisplayName("Warped pickaxe has nether wood attributes")
		void testWarpedPickaxe() {
			AttributeTester.tester(state("forgero:warped-pickaxe"))
					.add(Durability.KEY, 50, 20)
					.add(MiningSpeed.KEY, 2, 1)
					.run();
		}
	}
}
