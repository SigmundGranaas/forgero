package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
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
 * Integration tests for upgrade attribute filtering in the full attribute resolution pipeline.
 *
 * <p>This test was created to catch a critical bug where part-composite context
 * attributes were incorrectly being applied when materials were used as upgrades.
 * For example, iron used as a reinforcement was adding +10 damage instead of +2.</p>
 *
 * <p>The bug was in {@link AttributeContext#matchesSlotScope} which allowed
 * part-composite attributes to pass through upgrade slots.</p>
 *
 * @see AttributeContext#matchesSlotScope
 * @see CompositeAttributeBakingStrategy#collectUpgradesFromComponent
 */
@DisplayName("Upgrade Attribute Filtering Integration Tests")
class UpgradeAttributeFilteringIntegrationTest extends ForgeroTest {

	private static final OpenIdentifier OFFENSIVE_CONTEXT = new OpenIdentifier("forgero", "contexts/offensive");
	private static final OpenIdentifier UPGRADE_MATERIAL_TYPE = new OpenIdentifier("forgero", "materials/roles/upgrade_material");

	/**
	 * Creates an upgrade slot with the given context.
	 */
	private ComponentUpgradeSlot upgradeSlotWithContext(String id, OpenIdentifier context) {
		return new ComponentUpgradeSlot(
				id(id),
				UPGRADE_MATERIAL_TYPE,
				"",
				Optional.of(context),
				SlotValidator.ACCEPT_ALL,
				Optional.empty()
		);
	}

	/**
	 * Creates an upgrade slot with no context filter.
	 */
	private ComponentUpgradeSlot upgradeSlotWithoutContext(String id) {
		return new ComponentUpgradeSlot(
				id(id),
				UPGRADE_MATERIAL_TYPE,
				"",
				Optional.empty(),
				SlotValidator.ACCEPT_ALL,
				Optional.empty()
		);
	}

	@Nested
	@DisplayName("Part-Composite Attributes Should NOT Apply to Upgrades")
	class PartCompositeFiltering {

		@Test
		@DisplayName("CRITICAL BUG TEST: Iron reinforcement should not add part-composite damage")
		void ironReinforcementShouldNotAddPartCompositeDamage() {
			// Simulate iron material with part-composite attack_damage (like real iron.json)
			// This attribute should ONLY apply during part composition, NOT as upgrade
			Attribute partCompositeDamage = SimpleAttribute.withScope(
					DefaultAttributes.ATTACK_DAMAGE, 4.0f, AdditionOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);

			// Also add upgrade-context damage (from metal_upgrade_base inheritance)
			Attribute offensiveDamage = SimpleAttribute.withScope(
					DefaultAttributes.ATTACK_DAMAGE, 2.0f, AdditionOperator.getInstance(),
					OFFENSIVE_CONTEXT);

			Component ironUpgrade = part("iron-reinforcement")
					.withTag("materials/roles/upgrade_material")
					.withAttribute(partCompositeDamage)  // 4.0 - should NOT apply
					.withAttribute(offensiveDamage)       // 2.0 - should apply
					.build();

			// Create pickaxe head with reinforcement slot (offensive context)
			Component pickaxeHead = part("iron-pickaxe_head")
					.withTag("parts/types/pickaxe_head")
					.withUpgradeSlot(upgradeSlotWithContext("reinforcement", OFFENSIVE_CONTEXT)
							.withContent(ironUpgrade))
					.build();

			AttributeQueryResult result = resolveAttributes(pickaxeHead);
			float attackDamage = result.getValue(DefaultAttributes.ATTACK_DAMAGE);

			// Should get ONLY the 2.0 from offensive context, NOT the 4.0 from part-composite
			assertEquals(2.0f, attackDamage, 0.001f,
					"Iron reinforcement should add only 2.0 damage (offensive context), not 4.0+2.0=6.0 (bug was adding part-composite too)");
		}

		@Test
		@DisplayName("Part-composite attributes should never pass through even with no slot context")
		void partCompositeNeverPassesWithNoSlotContext() {
			// Material with only part-composite attributes
			Attribute partCompositeDamage = SimpleAttribute.withScope(
					DefaultAttributes.ATTACK_DAMAGE, 10.0f, AdditionOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);

			Component upgrade = part("test-upgrade")
					.withAttribute(partCompositeDamage)
					.build();

			// Create part with upgrade slot that has NO context filter
			Component head = part("test-head")
					.withUpgradeSlot(upgradeSlotWithoutContext("gem_slot")
							.withContent(upgrade))
					.build();

			AttributeQueryResult result = resolveAttributes(head);
			float attackDamage = result.getValue(DefaultAttributes.ATTACK_DAMAGE);

			// Part-composite should NOT pass through, even with no slot context
			assertEquals(0.0f, attackDamage, 0.001f,
					"Part-composite attributes should NEVER pass through upgrade slots");
		}
	}

	@Nested
	@DisplayName("UPGRADE Context Attributes Should Apply")
	class UpgradeContextApplies {

		@Test
		@DisplayName("Upgrade context attributes apply to any upgrade slot")
		void upgradeContextApplies() {
			Attribute upgradeBonus = SimpleAttribute.withScope(
					DefaultAttributes.ATTACK_DAMAGE, 5.0f, AdditionOperator.getInstance(),
					AttributeScope.UPGRADE);

			Component gem = part("diamond-gem")
					.withTag("upgrades/types/gem")
					.withAttribute(upgradeBonus)
					.build();

			// Slot with no context filter
			Component head = part("test-head")
					.withUpgradeSlot(upgradeSlotWithoutContext("gem_slot")
							.withContent(gem))
					.build();

			AttributeQueryResult result = resolveAttributes(head);

			assertEquals(5.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f,
					"UPGRADE context attributes should always apply in upgrade slots");
		}

		@Test
		@DisplayName("Upgrade context attributes apply even when slot has specific context")
		void upgradeContextAppliesToContextFilteredSlot() {
			Attribute upgradeBonus = SimpleAttribute.withScope(
					DefaultAttributes.DURABILITY, 100.0f, AdditionOperator.getInstance(),
					AttributeScope.UPGRADE);

			Component upgrade = part("test-upgrade")
					.withAttribute(upgradeBonus)
					.build();

			// Slot with offensive context filter
			Component head = part("test-head")
					.withUpgradeSlot(upgradeSlotWithContext("reinforcement", OFFENSIVE_CONTEXT)
							.withContent(upgrade))
					.build();

			AttributeQueryResult result = resolveAttributes(head);

			assertEquals(100.0f, result.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"UPGRADE context should apply even when slot has different context");
		}
	}

	@Nested
	@DisplayName("No-Context Attributes Should Apply")
	class NoContextApplies {

		@Test
		@DisplayName("Attributes with no context always apply")
		void noContextAlwaysApplies() {
			// Simple attribute with no context
			Attribute unconditionalBonus = new SimpleAttribute(DefaultAttributes.MINING_SPEED, 2.0f);

			Component upgrade = part("test-upgrade")
					.withAttribute(unconditionalBonus)
					.build();

			Component head = part("test-head")
					.withUpgradeSlot(upgradeSlotWithoutContext("slot")
							.withContent(upgrade))
					.build();

			AttributeQueryResult result = resolveAttributes(head);

			assertEquals(2.0f, result.getValue(DefaultAttributes.MINING_SPEED), 0.001f,
					"No-context attributes should always apply");
		}
	}

	@Nested
	@DisplayName("Specific Context Filtering")
	class SpecificContextFiltering {

		@Test
		@DisplayName("Offensive context attribute applies when slot has offensive context")
		void offensiveContextMatchesOffensiveSlot() {
			Attribute offensiveBonus = SimpleAttribute.withScope(
					DefaultAttributes.ATTACK_DAMAGE, 3.0f, AdditionOperator.getInstance(),
					OFFENSIVE_CONTEXT);

			Component upgrade = part("test-upgrade")
					.withAttribute(offensiveBonus)
					.build();

			Component head = part("test-head")
					.withUpgradeSlot(upgradeSlotWithContext("slot", OFFENSIVE_CONTEXT)
							.withContent(upgrade))
					.build();

			AttributeQueryResult result = resolveAttributes(head);

			assertEquals(3.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f,
					"Offensive context should match slot with offensive context");
		}

		@Test
		@DisplayName("Offensive context attribute does NOT apply when slot has no context")
		void offensiveContextDoesNotApplyToNoContextSlot() {
			Attribute offensiveBonus = SimpleAttribute.withScope(
					DefaultAttributes.ATTACK_DAMAGE, 3.0f, AdditionOperator.getInstance(),
					OFFENSIVE_CONTEXT);

			Component upgrade = part("test-upgrade")
					.withAttribute(offensiveBonus)
					.build();

			// Slot WITHOUT context filter
			Component head = part("test-head")
					.withUpgradeSlot(upgradeSlotWithoutContext("slot")
							.withContent(upgrade))
					.build();

			AttributeQueryResult result = resolveAttributes(head);

			assertEquals(0.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f,
					"Offensive context should NOT apply when slot has no context filter");
		}
	}

	@Nested
	@DisplayName("Structured Upgrade Composition (Guards, etc.)")
	class StructuredUpgradeComposition {

		@Test
		@DisplayName("CRITICAL: Structured upgrade (guard) has attributes composed before application")
		void structuredUpgradeComposesInternally() {
			// Create a guard-like structured upgrade: shape + material
			// Material provides base durability
			Attribute materialDurability = SimpleAttribute.withScope(
					DefaultAttributes.DURABILITY, 170.0f, AdditionOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);

			Component ironMaterial = part("iron-material")
					.withTag("materials/roles/tool_material")
					.withAttribute(materialDurability)
					.build();

			// Shape provides multiplier
			Attribute shapeMultiplier = SimpleAttribute.withScope(
					DefaultAttributes.DURABILITY, 0.1f,
					com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);

			Component guardShape = part("sword_guard_shape")
					.withTag("shapes/sword_guard")
					.withAttribute(shapeMultiplier)
					.build();

			// Create structured guard from material + shape
			Component ironGuard = part("iron-sword_guard")
					.withTag("parts/types/guard")
					.withStructureSlot(structureSlot("material", id("material_slot"), ironMaterial))
					.withStructureSlot(structureSlot("shape", id("shape_slot"), guardShape))
					.build();

			// Create sword with guard upgrade slot
			Component sword = part("golden-sword")
					.withTag("tools/sword")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 91.0f))
					.withUpgradeSlot(upgradeSlotWithoutContext("guard_slot")
							.withContent(ironGuard))
					.build();

			AttributeQueryResult result = resolveAttributes(sword);
			float durability = result.getValue(DefaultAttributes.DURABILITY);

			// Expected: 91 (sword base) + 17 (guard: 170 × 0.1 = 17 from composition)
			// Bug gave: 91 (guard's part-composite attrs were filtered out)
			assertEquals(108.0f, durability, 0.001f,
					"Sword should be 91 (base) + 17 (guard composed: 170×0.1), not just 91 (bug filtered out guard)");
		}

		@Test
		@DisplayName("Structured upgrade's composed attributes have NO context (pass through)")
		void structuredUpgradeComposedAttributesHaveNoContext() {
			// If guard's internal composition produces attributes with no context,
			// they should pass through the upgrade slot filtering

			Attribute materialMiningSpeed = SimpleAttribute.withScope(
					DefaultAttributes.MINING_SPEED, 6.0f, AdditionOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);

			Component material = part("iron-material")
					.withAttribute(materialMiningSpeed)
					.build();

			Attribute shapeMultiplier = SimpleAttribute.withScope(
					DefaultAttributes.MINING_SPEED, 1.2f,
					com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);

			Component shape = part("head_shape")
					.withAttribute(shapeMultiplier)
					.build();

			Component structuredPart = part("iron-head")
					.withStructureSlot(structureSlot("material", id("material_slot"), material))
					.withStructureSlot(structureSlot("shape", id("shape_slot"), shape))
					.build();

			Component tool = part("tool")
					.withUpgradeSlot(upgradeSlotWithoutContext("part_slot")
							.withContent(structuredPart))
					.build();

			AttributeQueryResult result = resolveAttributes(tool);
			float miningSpeed = result.getValue(DefaultAttributes.MINING_SPEED);

			// Composed: 6.0 × 1.2 = 7.2
			assertEquals(7.2f, miningSpeed, 0.001f,
					"Structured upgrade should contribute its composed mining speed");
		}

		@Test
		@DisplayName("Multiple structured upgrades each contribute their composed values")
		void multipleStructuredUpgradesStack() {
			// First guard: 170 × 0.1 = 17 durability
			Attribute mat1Dur = SimpleAttribute.withScope(
					DefaultAttributes.DURABILITY, 170.0f, AdditionOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);
			Component mat1 = part("iron-material-1").withAttribute(mat1Dur).build();

			Attribute shape1Mult = SimpleAttribute.withScope(
					DefaultAttributes.DURABILITY, 0.1f,
					com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);
			Component shape1 = part("guard_shape_1").withAttribute(shape1Mult).build();

			Component guard1 = part("iron-guard")
					.withStructureSlot(structureSlot("material", id("mat_slot"), mat1))
					.withStructureSlot(structureSlot("shape", id("shape_slot"), shape1))
					.build();

			// Second guard: 1550 × 0.1 = 155 durability (diamond)
			Attribute mat2Dur = SimpleAttribute.withScope(
					DefaultAttributes.DURABILITY, 1550.0f, AdditionOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);
			Component mat2 = part("diamond-material").withAttribute(mat2Dur).build();

			Attribute shape2Mult = SimpleAttribute.withScope(
					DefaultAttributes.DURABILITY, 0.1f,
					com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);
			Component shape2 = part("guard_shape_2").withAttribute(shape2Mult).build();

			Component guard2 = part("diamond-guard")
					.withStructureSlot(structureSlot("material", id("mat_slot"), mat2))
					.withStructureSlot(structureSlot("shape", id("shape_slot"), shape2))
					.build();

			// Sword with both guards
			Component sword = part("sword")
					.withAttribute(new SimpleAttribute(DefaultAttributes.DURABILITY, 100.0f))
					.withUpgradeSlot(upgradeSlotWithoutContext("guard_slot_1").withContent(guard1))
					.withUpgradeSlot(upgradeSlotWithoutContext("guard_slot_2").withContent(guard2))
					.build();

			AttributeQueryResult result = resolveAttributes(sword);
			float durability = result.getValue(DefaultAttributes.DURABILITY);

			// Expected: 100 (base) + 17 (iron guard) + 155 (diamond guard) = 272
			assertEquals(272.0f, durability, 0.001f,
					"Sword should get durability from both structured guards");
		}
	}

	@Nested
	@DisplayName("Real-World Scenario: Iron Pickaxe with Reinforcement")
	class RealWorldScenario {

		@Test
		@DisplayName("Full scenario: Iron pickaxe head with iron reinforcement")
		void fullIronPickaxeWithReinforcement() {
			// Iron material for pickaxe head (structure slot) - part-composite SHOULD apply here
			Attribute ironHeadDamage = SimpleAttribute.withScope(
					DefaultAttributes.ATTACK_DAMAGE, 4.0f, AdditionOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);

			Component ironMaterial = part("iron-material")
					.withTag("materials/roles/tool_material")
					.withAttribute(ironHeadDamage)
					.build();

			// Iron used as reinforcement (upgrade slot) - part-composite should NOT apply
			Attribute ironUpgradePartComposite = SimpleAttribute.withScope(
					DefaultAttributes.ATTACK_DAMAGE, 4.0f, AdditionOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);  // Should NOT apply
			Attribute ironUpgradeOffensive = SimpleAttribute.withScope(
					DefaultAttributes.ATTACK_DAMAGE, 2.0f, AdditionOperator.getInstance(),
					OFFENSIVE_CONTEXT);  // Should apply

			Component ironReinforcement = part("iron-reinforcement")
					.withTag("materials/roles/upgrade_material")
					.withAttribute(ironUpgradePartComposite)
					.withAttribute(ironUpgradeOffensive)
					.build();

			// Pickaxe head shape with multiplier
			Attribute damageMultiplier = SimpleAttribute.withScope(
					DefaultAttributes.ATTACK_DAMAGE, 1.0f,
					com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator.getInstance(),
					AttributeScope.PART_COMPOSITE);

			Component pickaxeHeadShape = part("pickaxe_head_shape")
					.withAttribute(damageMultiplier)
					.build();

			// Build pickaxe head with material, shape, and reinforcement
			Component pickaxeHead = part("iron-pickaxe_head")
					.withTag("parts/types/pickaxe_head")
					.withStructureSlot(structureSlot("material", id("material_slot"), ironMaterial))
					.withStructureSlot(structureSlot("shape", id("shape_slot"), pickaxeHeadShape))
					.withUpgradeSlot(upgradeSlotWithContext("reinforcement", OFFENSIVE_CONTEXT)
							.withContent(ironReinforcement))
					.build();

			AttributeQueryResult result = resolveAttributes(pickaxeHead);
			float attackDamage = result.getValue(DefaultAttributes.ATTACK_DAMAGE);

			// Expected: 4.0 (from iron material via part-composite) + 2.0 (from reinforcement via offensive) = 6.0
			// Bug would give: 4.0 + 4.0 + 2.0 = 10.0 (if part-composite leaked through upgrade)
			assertEquals(6.0f, attackDamage, 0.001f,
					"Attack damage should be 4.0 (iron head) + 2.0 (reinforcement offensive), not 10.0 (if part-composite leaked)");
		}
	}
}
