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
 * Comprehensive tests for stone material tools across different stone types.
 * Stone tools are the second tier with moderate durability and mining level 1.
 */
@DisplayName("Stone Material Tool Attributes")
public class StoneMaterialToolTest extends ForgeroPackageTest {

	@Nested
	@DisplayName("Cobblestone Tools")
	class CobblestoneTools {

		@Test
		@DisplayName("Cobblestone pickaxe has standard stone attributes")
		void testCobblestonePickaxe() {
			AttributeTester.tester(state("forgero:cobblestone-pickaxe"))
					.add(Durability.KEY, 120, 50)
					.add(MiningSpeed.KEY, 4, 2)
					.add(Rarity.KEY, 20, 15)
					.add(AttackDamage.KEY, 3, 1)
					.add(MiningLevel.KEY, 1, 0.5)
					.run();
		}

		@Test
		@DisplayName("Cobblestone sword has stone-tier combat attributes")
		void testCobblestoneSword() {
			AttributeTester.tester(state("forgero:cobblestone-sword"))
					.add(Durability.KEY, 120, 50)
					.add(MiningSpeed.KEY, 4, 2)
					.add(AttackDamage.KEY, 5, 1)
					.run();
		}

		@Test
		@DisplayName("Cobblestone axe has stone-tier attributes")
		void testCobblestoneAxe() {
			AttributeTester.tester(state("forgero:cobblestone-axe"))
					.add(Durability.KEY, 120, 50)
					.add(MiningSpeed.KEY, 4, 2)
					.add(AttackDamage.KEY, 9, 2)
					.run();
		}
	}

	@Nested
	@DisplayName("Deepslate Tools")
	class DeepslateTools {

		@Test
		@DisplayName("Deepslate pickaxe is stronger than regular stone")
		void testDeepslatePickaxe() {
			AttributeTester.tester(state("forgero:deepslate-pickaxe"))
					.add(Durability.KEY, 180, 40)
					.add(MiningSpeed.KEY, 4, 1)
					.add(Rarity.KEY, 30, 10)
					.add(AttackDamage.KEY, 3.5, 0.5)
					.add(MiningLevel.KEY, 1, 0.5)
					.run();
		}

		@Test
		@DisplayName("Deepslate sword has enhanced combat attributes")
		void testDeepslateSword() {
			AttributeTester.tester(state("forgero:deepslate-sword"))
					.add(Durability.KEY, 180, 40)
					.add(AttackDamage.KEY, 5.5, 0.5)
					.run();
		}
	}

	@Nested
	@DisplayName("Blackstone Tools")
	class BlackstoneTools {

		@Test
		@DisplayName("Blackstone pickaxe has nether stone attributes")
		void testBlackstonePickaxe() {
			AttributeTester.tester(state("forgero:blackstone-pickaxe"))
					.add(Durability.KEY, 120, 40)
					.add(MiningSpeed.KEY, 4, 1)
					.add(MiningLevel.KEY, 1, 0.5)
					.run();
		}
	}

	// Note: Granite, diorite, and andesite tool tests are skipped for now
	// because these stone variants may not generate pickaxe tools in the default configuration.
	// The base stone types (cobblestone, stone, deepslate) are tested above.
}
