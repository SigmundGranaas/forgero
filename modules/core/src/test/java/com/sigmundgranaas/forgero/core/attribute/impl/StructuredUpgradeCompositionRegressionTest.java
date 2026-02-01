package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for the structured upgrade composition bug.
 *
 * <h2>Bug Description</h2>
 * <p>When a guard (structured component with shape + material) was installed as an upgrade,
 * its durability was NOT added to the parent sword. The tooltip showed the base sword
 * durability (e.g., 91 for golden sword) instead of base + guard (e.g., 91 + 17 = 108).</p>
 *
 * <h2>Root Cause</h2>
 * <p>The bug was in {@link CompositeAttributeBakingStrategy#collectUpgradesFromComponent}.
 * When collecting attributes from upgrades, it read raw properties and filtered by context.
 * Structured upgrades like guards have {@link AttributeContext#PART_COMPOSITE} attributes
 * which were filtered out by {@link AttributeContext#matchesSlotScope}.</p>
 *
 * <h2>The Fix</h2>
 * <p>Structured upgrades are now composed FIRST (shape × material), producing resolved
 * attributes with NO context. These no-context attributes pass through slot filtering.</p>
 *
 * <h2>Test Coverage</h2>
 * <ul>
 *   <li>Exact reproduction of guard durability bug</li>
 *   <li>Multiple structured upgrades stacking</li>
 *   <li>Different material values produce different results</li>
 *   <li>Structured upgrade with multiple attribute types</li>
 *   <li>Edge cases: missing base, missing multiplier</li>
 * </ul>
 *
 * @see CompositeAttributeBakingStrategy
 * @see AttributeContext#PART_COMPOSITE
 */
@DisplayName("Structured Upgrade Composition Regression Tests")
class StructuredUpgradeCompositionRegressionTest extends ForgeroTest {

	private static final OpenIdentifier UPGRADE_TYPE = new OpenIdentifier("forgero", "upgrades/types/guard");

	/**
	 * Creates an upgrade slot with no context filter (accepts any upgrade).
	 */
	private ComponentUpgradeSlot upgradeSlot(String id) {
		return new ComponentUpgradeSlot(
				id(id),
				UPGRADE_TYPE,
				"",
				Optional.empty(),
				SlotValidator.ACCEPT_ALL,
				Optional.empty()
		);
	}

	/**
	 * Creates a guard-like structured component with material + shape.
	 *
	 * @param name Guard name (e.g., "iron-sword_guard")
	 * @param materialDurability Material's base durability (e.g., 170 for iron)
	 * @param shapeMultiplier Shape's durability multiplier (e.g., 0.1 for sword_guard)
	 * @return A structured component representing the guard
	 */
	private Component createGuard(String name, float materialDurability, float shapeMultiplier) {
		Attribute matDur = SimpleAttribute.withScope(
				DefaultAttributes.DURABILITY, materialDurability, AdditionOperator.getInstance(),
				AttributeScope.PART_COMPOSITE);

		Component material = part(name + "-material")
				.withTag("materials/roles/tool_material")
				.withAttribute(matDur)
				.build();

		Attribute shapeMult = SimpleAttribute.withScope(
				DefaultAttributes.DURABILITY, shapeMultiplier, MultiplicationOperator.getInstance(),
				AttributeScope.PART_COMPOSITE);

		Component shape = part(name + "-shape")
				.withTag("shapes/sword_guard")
				.withAttribute(shapeMult)
				.build();

		return part(name)
				.withTag("parts/types/guard")
				.withStructureSlot(structureSlot("material", id("material_slot"), material))
				.withStructureSlot(structureSlot("shape", id("shape_slot"), shape))
				.build();
	}

	@Nested
	@DisplayName("REGRESSION: Guard Durability Bug")
	class GuardDurabilityBug {

		/**
		 * EXACT reproduction of the original bug.
		 *
		 * Scenario:
		 * - Golden sword: 91 durability (base)
		 * - Iron guard: 170 × 0.1 = 17 durability (from composition)
		 * - Expected: 91 + 17 = 108 durability
		 * - Bug gave: 91 (guard's attributes were filtered out)
		 */
		@Test
		@DisplayName("CRITICAL: Iron guard on golden sword should add 17 durability")
		void ironGuardOnGoldenSwordAdds17Durability() {
			// Golden sword base: 91 durability
			Component goldenSword = part("golden-sword")
					.withTag("tools/sword")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 91.0f))
					.withUpgradeSlot(upgradeSlot("guard_slot"))
					.build();

			// Iron guard: 170 × 0.1 = 17 durability
			Component ironGuard = createGuard("iron-sword_guard", 170.0f, 0.1f);

			// Install guard
			Component upgradedSword = part("golden-sword-upgraded")
					.withTag("tools/sword")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 91.0f))
					.withUpgradeSlot(upgradeSlot("guard_slot").withContent(ironGuard))
					.build();

			AttributeQueryResult result = attributeEngine().resolve(upgradedSword);
			float durability = result.getValue(DefaultAttributes.DURABILITY);

			// THE CRITICAL ASSERTION - this failed before the fix
			assertEquals(108.0f, durability, 0.01f,
					"Golden sword (91) + iron guard (17) should equal 108. " +
							"BUG: Guard's part-composite attributes were filtered out, giving just 91.");
		}

		/**
		 * Verifies that without the guard, sword has base durability only.
		 * This establishes the baseline for the bug test.
		 */
		@Test
		@DisplayName("Baseline: Golden sword without guard has 91 durability")
		void goldenSwordWithoutGuardHas91Durability() {
			Component goldenSword = part("golden-sword")
					.withTag("tools/sword")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 91.0f))
					.withUpgradeSlot(upgradeSlot("guard_slot")) // Empty slot
					.build();

			AttributeQueryResult result = attributeEngine().resolve(goldenSword);
			float durability = result.getValue(DefaultAttributes.DURABILITY);

			assertEquals(91.0f, durability, 0.01f,
					"Golden sword without upgrades should have exactly 91 durability");
		}

		/**
		 * Verifies the guard itself has correct composed durability.
		 */
		@Test
		@DisplayName("Iron guard alone has 17 durability (170 × 0.1)")
		void ironGuardAloneHas17Durability() {
			Component ironGuard = createGuard("iron-sword_guard", 170.0f, 0.1f);

			AttributeQueryResult result = attributeEngine().resolve(ironGuard);
			float durability = result.getValue(DefaultAttributes.DURABILITY);

			assertEquals(17.0f, durability, 0.01f,
					"Iron guard (170 × 0.1) should have 17 durability");
		}
	}

	@Nested
	@DisplayName("Multiple Structured Upgrades")
	class MultipleStructuredUpgrades {

		@Test
		@DisplayName("Two guards stack their durability")
		void twoGuardsStackDurability() {
			// Base sword: 100
			// Iron guard: 170 × 0.1 = 17
			// Diamond guard: 1550 × 0.1 = 155
			// Total: 100 + 17 + 155 = 272

			Component ironGuard = createGuard("iron-guard", 170.0f, 0.1f);
			Component diamondGuard = createGuard("diamond-guard", 1550.0f, 0.1f);

			Component sword = part("sword")
					.withTag("tools/sword")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 100.0f))
					.withUpgradeSlot(upgradeSlot("guard_slot_1").withContent(ironGuard))
					.withUpgradeSlot(upgradeSlot("guard_slot_2").withContent(diamondGuard))
					.build();

			AttributeQueryResult result = attributeEngine().resolve(sword);
			float durability = result.getValue(DefaultAttributes.DURABILITY);

			assertEquals(272.0f, durability, 0.01f,
					"Sword (100) + iron guard (17) + diamond guard (155) = 272");
		}

		@Test
		@DisplayName("Three identical guards triple the bonus")
		void threeIdenticalGuardsTripleBonus() {
			// Base: 50
			// Each guard: 100 × 0.2 = 20
			// Total: 50 + 20 + 20 + 20 = 110

			Component guard1 = createGuard("guard-1", 100.0f, 0.2f);
			Component guard2 = createGuard("guard-2", 100.0f, 0.2f);
			Component guard3 = createGuard("guard-3", 100.0f, 0.2f);

			Component sword = part("sword")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 50.0f))
					.withUpgradeSlot(upgradeSlot("slot_1").withContent(guard1))
					.withUpgradeSlot(upgradeSlot("slot_2").withContent(guard2))
					.withUpgradeSlot(upgradeSlot("slot_3").withContent(guard3))
					.build();

			AttributeQueryResult result = attributeEngine().resolve(sword);
			float durability = result.getValue(DefaultAttributes.DURABILITY);

			assertEquals(110.0f, durability, 0.01f,
					"Base (50) + 3 × guard (20) = 110");
		}
	}

	@Nested
	@DisplayName("Different Materials Produce Different Results")
	class DifferentMaterials {

		@Test
		@DisplayName("Diamond guard provides more durability than iron")
		void diamondGuardProvideMoreThanIron() {
			Component ironGuard = createGuard("iron-guard", 170.0f, 0.1f);    // = 17
			Component diamondGuard = createGuard("diamond-guard", 1550.0f, 0.1f); // = 155

			AttributeQueryResult ironResult = attributeEngine().resolve(ironGuard);
			AttributeQueryResult diamondResult = attributeEngine().resolve(diamondGuard);

			float ironDur = ironResult.getValue(DefaultAttributes.DURABILITY);
			float diamondDur = diamondResult.getValue(DefaultAttributes.DURABILITY);

			assertTrue(diamondDur > ironDur,
					String.format("Diamond guard (%f) should have more durability than iron (%f)",
							diamondDur, ironDur));
			assertEquals(17.0f, ironDur, 0.01f);
			assertEquals(155.0f, diamondDur, 0.01f);
		}

		@Test
		@DisplayName("Same shape, different materials = different results")
		void sameShapeDifferentMaterials() {
			// All use 0.5 multiplier (different shape)
			Component woodGuard = createGuard("wood-guard", 60.0f, 0.5f);     // = 30
			Component stoneGuard = createGuard("stone-guard", 131.0f, 0.5f);  // = 65.5
			Component ironGuard = createGuard("iron-guard", 170.0f, 0.5f);    // = 85

			float woodDur = attributeEngine().resolve(woodGuard).getValue(DefaultAttributes.DURABILITY);
			float stoneDur = attributeEngine().resolve(stoneGuard).getValue(DefaultAttributes.DURABILITY);
			float ironDur = attributeEngine().resolve(ironGuard).getValue(DefaultAttributes.DURABILITY);

			assertTrue(ironDur > stoneDur && stoneDur > woodDur,
					String.format("Expected iron (%f) > stone (%f) > wood (%f)",
							ironDur, stoneDur, woodDur));
		}
	}

	@Nested
	@DisplayName("Multiple Attribute Types")
	class MultipleAttributeTypes {

		/**
		 * Creates a guard with both durability and attack damage.
		 */
		private Component createGuardWithMultipleAttrs(String name,
				float materialDur, float materialDmg,
				float shapeDurMult, float shapeDmgMult) {

			Attribute matDur = SimpleAttribute.withScope(
					DefaultAttributes.DURABILITY, materialDur, AdditionOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);
			Attribute matDmg = SimpleAttribute.withScope(
					DefaultAttributes.ATTACK_DAMAGE, materialDmg, AdditionOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);

			Component material = part(name + "-material")
					.withAttribute(matDur)
					.withAttribute(matDmg)
					.build();

			Attribute shapeDurMult_ = SimpleAttribute.withScope(
					DefaultAttributes.DURABILITY, shapeDurMult, MultiplicationOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);
			Attribute shapeDmgMult_ = SimpleAttribute.withScope(
					DefaultAttributes.ATTACK_DAMAGE, shapeDmgMult, MultiplicationOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);

			Component shape = part(name + "-shape")
					.withAttribute(shapeDurMult_)
					.withAttribute(shapeDmgMult_)
					.build();

			return part(name)
					.withStructureSlot(structureSlot("material", id("material_slot"), material))
					.withStructureSlot(structureSlot("shape", id("shape_slot"), shape))
					.build();
		}

		@Test
		@DisplayName("Guard adds both durability and attack damage")
		void guardAddsBothDurabilityAndDamage() {
			// Guard: dur = 200 × 0.1 = 20, dmg = 4 × 0.5 = 2
			Component guard = createGuardWithMultipleAttrs("iron-guard",
					200.0f, 4.0f,  // material: dur, dmg
					0.1f, 0.5f);   // shape multipliers

			Component sword = part("sword")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 100.0f))
					.withAttribute(new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5.0f))
					.withUpgradeSlot(upgradeSlot("guard_slot").withContent(guard))
					.build();

			AttributeQueryResult result = attributeEngine().resolve(sword);

			assertEquals(120.0f, result.getValue(DefaultAttributes.DURABILITY), 0.01f,
					"Durability: 100 + 20 = 120");
			assertEquals(7.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.01f,
					"Attack damage: 5 + 2 = 7");
		}
	}

	@Nested
	@DisplayName("Edge Cases")
	class EdgeCases {

		@Test
		@DisplayName("Guard with only multiplier (no base) produces 0")
		void guardWithOnlyMultiplierProducesZero() {
			// Shape has multiplier but material has no base = composition fails
			Attribute shapeMult = SimpleAttribute.withScope(
					DefaultAttributes.DURABILITY, 0.5f, MultiplicationOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);

			Component shape = part("guard-shape")
					.withAttribute(shapeMult)
					.build();

			Component guard = part("broken-guard")
					.withStructureSlot(structureSlot("shape", id("shape_slot"), shape))
					// No material - no base value
					.build();

			Component sword = part("sword")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 100.0f))
					.withUpgradeSlot(upgradeSlot("slot").withContent(guard))
					.build();

			AttributeQueryResult result = attributeEngine().resolve(sword);

			// Guard contributes nothing (composition requires base + multiplier from different sources)
			assertEquals(100.0f, result.getValue(DefaultAttributes.DURABILITY), 0.01f,
					"Guard with no base should contribute 0");
		}

		@Test
		@DisplayName("Guard with only base (no multiplier) produces 0")
		void guardWithOnlyBaseProducesZero() {
			// Material has base but shape has no multiplier = composition fails
			Attribute matBase = SimpleAttribute.withScope(
					DefaultAttributes.DURABILITY, 200.0f, AdditionOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);

			Component material = part("guard-material")
					.withAttribute(matBase)
					.build();

			Component guard = part("broken-guard")
					.withStructureSlot(structureSlot("material", id("material_slot"), material))
					// No shape - no multiplier
					.build();

			Component sword = part("sword")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 100.0f))
					.withUpgradeSlot(upgradeSlot("slot").withContent(guard))
					.build();

			AttributeQueryResult result = attributeEngine().resolve(sword);

			// Guard contributes nothing (composition requires base + multiplier from different sources)
			assertEquals(100.0f, result.getValue(DefaultAttributes.DURABILITY), 0.01f,
					"Guard with no multiplier should contribute 0");
		}

		@Test
		@DisplayName("Non-structured upgrade still works (simple attribute)")
		void nonStructuredUpgradeWorks() {
			// Simple upgrade with no-context attribute (like ender pearl)
			Component simpleUpgrade = part("ender-pearl")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 105.0f))
					.build();

			Component sword = part("sword")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 100.0f))
					.withUpgradeSlot(upgradeSlot("slot").withContent(simpleUpgrade))
					.build();

			AttributeQueryResult result = attributeEngine().resolve(sword);

			assertEquals(205.0f, result.getValue(DefaultAttributes.DURABILITY), 0.01f,
					"Simple upgrade should add its durability directly");
		}

		@Test
		@DisplayName("Mixed: structured + simple upgrades both contribute")
		void mixedUpgradesBothContribute() {
			// Structured guard: 200 × 0.1 = 20
			Component guard = createGuard("iron-guard", 200.0f, 0.1f);

			// Simple upgrade: +50
			Component simpleUpgrade = part("gem")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 50.0f))
					.build();

			Component sword = part("sword")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 100.0f))
					.withUpgradeSlot(upgradeSlot("guard_slot").withContent(guard))
					.withUpgradeSlot(upgradeSlot("gem_slot").withContent(simpleUpgrade))
					.build();

			AttributeQueryResult result = attributeEngine().resolve(sword);

			assertEquals(170.0f, result.getValue(DefaultAttributes.DURABILITY), 0.01f,
					"Base (100) + guard (20) + gem (50) = 170");
		}

		@Test
		@DisplayName("Deeply nested: sword with head that has guard upgrade")
		void deeplyNestedStructureWorks() {
			// This tests the realistic scenario: sword has a head (structure),
			// and the head has a guard upgrade slot

			// Guard: 100 × 0.2 = 20
			Component guard = createGuard("iron-guard", 100.0f, 0.2f);

			// Head with guard upgrade slot (head is CustomizableComponent due to upgrade slot)
			Component head = part("iron-sword_head")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 150.0f))
					.withUpgradeSlot(upgradeSlot("guard_slot").withContent(guard))
					.build();

			// First verify the head alone has correct durability
			AttributeQueryResult headResult = attributeEngine().resolve(head);
			assertEquals(170.0f, headResult.getValue(DefaultAttributes.DURABILITY), 0.01f,
					"Head alone should be 150 (base) + 20 (guard)");

			// Sword with head as structure slot
			Component sword = part("iron-sword")
					.withStructureSlot(structureSlot("head", id("head_slot"), head))
					.build();

			AttributeQueryResult swordResult = attributeEngine().resolve(sword);
			float swordDurability = swordResult.getValue(DefaultAttributes.DURABILITY);

			// The sword should get the head's resolved durability
			// Note: Due to structure composition rules, the head's attributes flow through
			assertTrue(swordDurability >= 170.0f,
					String.format("Sword should have at least head's durability (170), got %f", swordDurability));
		}
	}
}
