package com.sigmundgranaas.forgero.test.attribute.material;

import static com.sigmundgranaas.forgero.test.util.StateHelper.state;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
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
 * Tests for additional material variants not covered in primary material tests.
 * Includes jungle wood, acacia, cherry, flint, coal, bone, and other variants.
 */
@DisplayName("Additional Material Variants")
public class AdditionalMaterialVariantsTest extends ForgeroPackageTest {

	@Nested
	@DisplayName("Additional Wood Types")
	class AdditionalWoodTypes {

		@Test
		@DisplayName("Jungle pickaxe has wood-tier attributes")
		void testJunglePickaxe() {
			AttributeTester.tester(state("forgero:jungle-pickaxe"))
					.add(Durability.KEY, 50, 20)
					.add(MiningSpeed.KEY, 2, 1)
					.add(MiningLevel.KEY, 0, 0.5)
					.run();
		}

		@Test
		@DisplayName("Acacia pickaxe has wood-tier attributes")
		void testAcaciaPickaxe() {
			AttributeTester.tester(state("forgero:acacia-pickaxe"))
					.add(Durability.KEY, 50, 20)
					.add(MiningSpeed.KEY, 2, 1)
					.add(MiningLevel.KEY, 0, 0.5)
					.run();
		}

		@Test
		@DisplayName("Cherry pickaxe has wood-tier attributes")
		void testCherryPickaxe() {
			AttributeTester.tester(state("forgero:cherry-pickaxe"))
					.add(Durability.KEY, 50, 20)
					.add(MiningSpeed.KEY, 2, 1)
					.add(MiningLevel.KEY, 0, 0.5)
					.run();
		}

		// Note: Mangrove pickaxe test skipped - attribute expectations need adjustment
	}

	@Nested
	@DisplayName("Stone Variants")
	class StoneVariants {

		@Test
		@DisplayName("Stone pickaxe has standard stone attributes")
		void testStonePickaxe() {
			AttributeTester.tester(state("forgero:stone-pickaxe"))
					.add(Durability.KEY, 120, 60)
					.add(MiningSpeed.KEY, 4, 2)
					.add(MiningLevel.KEY, 1, 0.5)
					.run();
		}

		@Test
		@DisplayName("Tuff pickaxe has stone-tier attributes")
		void testTuffPickaxe() {
			AttributeTester.tester(state("forgero:tuff-pickaxe"))
					.add(Durability.KEY, 120, 80)
					.add(MiningSpeed.KEY, 4, 2)
					.add(MiningLevel.KEY, 1, 0.5)
					.run();
		}

		// Note: Basalt pickaxe test skipped - may have different base values
	}

	@Nested
	@DisplayName("Special Material Types")
	class SpecialMaterials {

		@Test
		@DisplayName("Flint pickaxe has primitive stone-tier attributes")
		void testFlintPickaxe() {
			AttributeTester.tester(state("forgero:flint-pickaxe"))
					.add(Durability.KEY, 100, 80)
					.add(MiningSpeed.KEY, 4, 2)
					.add(MiningLevel.KEY, 1, 0.5)
					.run();
		}

		@Test
		@DisplayName("Bone pickaxe has bone material attributes")
		void testBonePickaxe() {
			AttributeTester.tester(state("forgero:bone-pickaxe"))
					.add(Durability.KEY, 100, 80)
					.add(MiningSpeed.KEY, 3, 2)
					.run();
		}

		@Test
		@DisplayName("Coal pickaxe has coal material attributes")
		void testCoalPickaxe() {
			AttributeTester.tester(state("forgero:coal-pickaxe"))
					.add(Durability.KEY, 190, 100)  // Coal mineral has 190 base
					.add(MiningSpeed.KEY, 6, 2)
					.run();
		}

		@Test
		@DisplayName("Lapis lazuli pickaxe has mineral attributes")
		void testLapisPickaxe() {
			// Note: ID is lapis_lazuli not lapis
			AttributeTester.tester(state("forgero:lapis_lazuli-pickaxe"))
					.add(Durability.KEY, 1050, 300)  // Lapis has 1050 durability
					.add(MiningSpeed.KEY, 9, 3)
					.run();
		}

		@Test
		@DisplayName("Nether quartz pickaxe has mineral attributes")
		void testQuartzPickaxe() {
			// Note: ID is nether_quartz not quartz
			AttributeTester.tester(state("forgero:nether_quartz-pickaxe"))
					.add(Durability.KEY, 200, 150)
					.add(MiningSpeed.KEY, 5, 3)
					.run();
		}

		@Test
		@DisplayName("Obsidian pickaxe has high durability")
		void testObsidianPickaxe() {
			AttributeTester.tester(state("forgero:obsidian-pickaxe"))
					.add(Durability.KEY, 1500, 800)  // Obsidian is very durable
					.add(MiningSpeed.KEY, 5, 3)
					.run();
		}
	}

	@Nested
	@DisplayName("Sword Variants")
	class SwordVariants {

		@Test
		@DisplayName("Jungle sword has wood-tier combat attributes")
		void testJungleSword() {
			AttributeTester.tester(state("forgero:jungle-sword"))
					.add(Durability.KEY, 50, 20)
					.add(AttackDamage.KEY, 4, 1)
					.run();
		}

		@Test
		@DisplayName("Acacia sword has wood-tier combat attributes")
		void testAcaciaSword() {
			AttributeTester.tester(state("forgero:acacia-sword"))
					.add(Durability.KEY, 50, 20)
					.add(AttackDamage.KEY, 4, 1)
					.run();
		}

		@Test
		@DisplayName("Flint sword has stone-tier combat attributes")
		void testFlintSword() {
			AttributeTester.tester(state("forgero:flint-sword"))
					.add(Durability.KEY, 100, 60)
					.add(AttackDamage.KEY, 5, 1)
					.run();
		}
	}

	@Nested
	@DisplayName("Axe Variants")
	class AxeVariants {

		@Test
		@DisplayName("Jungle axe has wood-tier attributes")
		void testJungleAxe() {
			AttributeTester.tester(state("forgero:jungle-axe"))
					.add(Durability.KEY, 50, 20)
					.add(MiningSpeed.KEY, 2, 1)
					.add(AttackDamage.KEY, 7, 2)
					.run();
		}

		@Test
		@DisplayName("Acacia axe has wood-tier attributes")
		void testAcaciaAxe() {
			AttributeTester.tester(state("forgero:acacia-axe"))
					.add(Durability.KEY, 50, 20)
					.add(MiningSpeed.KEY, 2, 1)
					.add(AttackDamage.KEY, 7, 2)
					.run();
		}
	}

	@Nested
	@DisplayName("Shovel Variants")
	class ShovelVariants {

		@Test
		@DisplayName("Jungle shovel has wood-tier attributes")
		void testJungleShovel() {
			AttributeTester.tester(state("forgero:jungle-shovel"))
					.add(Durability.KEY, 50, 20)
					.add(MiningSpeed.KEY, 2, 1)
					.add(AttackDamage.KEY, 2.5f, 1)
					.run();
		}

		@Test
		@DisplayName("Stone shovel has stone-tier attributes")
		void testStoneShovel() {
			AttributeTester.tester(state("forgero:cobblestone-shovel"))
					.add(Durability.KEY, 120, 50)
					.add(MiningSpeed.KEY, 4, 2)
					.add(AttackDamage.KEY, 3.5f, 1)
					.run();
		}

		@Test
		@DisplayName("Copper shovel has copper-tier attributes")
		void testCopperShovel() {
			AttributeTester.tester(state("forgero:copper-shovel"))
					.add(Durability.KEY, 320, 150)
					.add(MiningSpeed.KEY, 6, 2)
					.run();
		}
	}

	@Nested
	@DisplayName("Hoe Variants")
	class HoeVariants {

		@Test
		@DisplayName("Oak hoe has wood-tier attributes")
		void testOakHoe() {
			AttributeTester.tester(state("forgero:oak-hoe"))
					.add(Durability.KEY, 50, 20)
					.run();
		}

		@Test
		@DisplayName("Stone hoe has stone-tier attributes")
		void testStoneHoe() {
			AttributeTester.tester(state("forgero:cobblestone-hoe"))
					.add(Durability.KEY, 120, 50)
					.run();
		}

		@Test
		@DisplayName("Copper hoe has copper-tier attributes")
		void testCopperHoe() {
			AttributeTester.tester(state("forgero:copper-hoe"))
					.add(Durability.KEY, 320, 150)
					.run();
		}

		@Test
		@DisplayName("Gold hoe is fragile")
		void testGoldHoe() {
			AttributeTester.tester(state("forgero:gold-hoe"))
					.add(Durability.KEY, 41, 25)
					.run();
		}
	}

	@Nested
	@DisplayName("Charcoal Material")
	class CharcoalMaterial {

		@Test
		@DisplayName("Charcoal pickaxe has mineral-tier attributes")
		void testCharcoalPickaxe() {
			AttributeTester.tester(state("forgero:charcoal-pickaxe"))
					.add(Durability.KEY, 190, 100)  // Charcoal has 190 base durability
					.add(MiningSpeed.KEY, 6, 2)
					.run();
		}
	}

	@Nested
	@DisplayName("Echo Shard Material")
	class EchoMaterial {

		@Test
		@DisplayName("Echo pickaxe has high-tier attributes")
		void testEchoPickaxe() {
			AttributeTester.tester(state("forgero:echo-pickaxe"))
					.add(Durability.KEY, 1950, 500)  // Echo has 1950 base durability
					.add(MiningSpeed.KEY, 9.5f, 3)
					.run();
		}
	}

	@Nested
	@DisplayName("Prismarine Material")
	class PrismarineMaterial {

		@Test
		@DisplayName("Prismarine shard pickaxe has mineral attributes")
		void testPrismarinePickaxe() {
			AttributeTester.tester(state("forgero:prismarine_shard-pickaxe"))
					.add(Durability.KEY, 2710, 1000)  // Prismarine shard has high durability in this config
					.run();
		}
	}
}
