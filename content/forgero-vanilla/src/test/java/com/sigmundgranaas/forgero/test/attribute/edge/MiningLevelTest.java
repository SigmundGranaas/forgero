package com.sigmundgranaas.forgero.test.attribute.edge;

import static com.sigmundgranaas.forgero.test.util.StateHelper.state;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningLevel;
import com.sigmundgranaas.forgero.test.util.AttributeTester;
import com.sigmundgranaas.forgero.test.util.ForgeroPackageTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for mining level progression across different material tiers.
 * Validates that tools have the correct mining level for their material type.
 */
@DisplayName("Mining Level By Material Tier")
public class MiningLevelTest extends ForgeroPackageTest {

	@Nested
	@DisplayName("Wood Tier (Mining Level 0)")
	class WoodTier {

		@Test
		@DisplayName("Oak pickaxe has mining level 0")
		void testOakPickaxeMiningLevel() {
			AttributeTester.tester(state("forgero:oak-pickaxe"))
					.add(MiningLevel.KEY, 0, 0.5)
					.run();
		}

		@Test
		@DisplayName("Birch pickaxe has mining level 0")
		void testBirchPickaxeMiningLevel() {
			AttributeTester.tester(state("forgero:birch-pickaxe"))
					.add(MiningLevel.KEY, 0, 0.5)
					.run();
		}
	}

	@Nested
	@DisplayName("Stone Tier (Mining Level 1)")
	class StoneTier {

		@Test
		@DisplayName("Cobblestone pickaxe has mining level 1")
		void testCobblestonePickaxeMiningLevel() {
			AttributeTester.tester(state("forgero:cobblestone-pickaxe"))
					.add(MiningLevel.KEY, 1, 0.5)
					.run();
		}

		@Test
		@DisplayName("Gold pickaxe has mining level 1 (despite being metal)")
		void testGoldPickaxeMiningLevel() {
			AttributeTester.tester(state("forgero:gold-pickaxe"))
					.add(MiningLevel.KEY, 1, 0.5)
					.run();
		}
	}

	@Nested
	@DisplayName("Iron Tier (Mining Level 2)")
	class IronTier {

		@Test
		@DisplayName("Iron pickaxe has mining level 2")
		void testIronPickaxeMiningLevel() {
			AttributeTester.tester(state("forgero:iron-pickaxe"))
					.add(MiningLevel.KEY, 2, 0.5)
					.run();
		}

		@Test
		@DisplayName("Copper pickaxe has mining level 2")
		void testCopperPickaxeMiningLevel() {
			AttributeTester.tester(state("forgero:copper-pickaxe"))
					.add(MiningLevel.KEY, 2, 0.5)
					.run();
		}
	}

	@Nested
	@DisplayName("Diamond Tier (Mining Level 3)")
	class DiamondTier {

		@Test
		@DisplayName("Diamond pickaxe has mining level 3")
		void testDiamondPickaxeMiningLevel() {
			AttributeTester.tester(state("forgero:diamond-pickaxe"))
					.add(MiningLevel.KEY, 3, 0.5)
					.run();
		}

		@Test
		@DisplayName("Emerald pickaxe has mining level 3")
		void testEmeraldPickaxeMiningLevel() {
			AttributeTester.tester(state("forgero:emerald-pickaxe"))
					.add(MiningLevel.KEY, 3, 0.5)
					.run();
		}
	}

	@Nested
	@DisplayName("Netherite Tier (Mining Level 4)")
	class NetheriteTier {

		@Test
		@DisplayName("Netherite pickaxe has mining level 4")
		void testNetheritePickaxeMiningLevel() {
			AttributeTester.tester(state("forgero:netherite-pickaxe"))
					.add(MiningLevel.KEY, 4, 0.5)
					.run();
		}
	}
}
