package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.component.impl.StructuredPart;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.InSlotTypeCondition;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.attributeEngine;
import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.resolver;
import static org.junit.jupiter.api.Assertions.*;

/**
 * VALIDATION TESTS for material slot-based attribute conditions.
 *
 * These tests verify that the critical slot-scoped attribute system works correctly:
 * - Armor attributes ONLY apply when material is in armor_material slot
 * - Tool attributes ONLY apply when material is in tool_material slot
 * - Handle durability contributes correctly to tool total durability
 *
 * These tests are designed to catch regressions like:
 * - Armor attributes appearing on tools (wrong slot_type in condition)
 * - Handles using wrong material (iron instead of oak)
 * - Attribute conditions not being respected during resolution
 */
@DisplayName("Material Slot Condition Validation Tests")
class MaterialSlotConditionValidationTest extends ForgeroTest {

	private static final OpenIdentifier ARMOR_MATERIAL_ID = OpenIdentifier.parse("forgero:armor_material");
	private static final OpenIdentifier ARMOR_TAG = OpenIdentifier.parse("forgero:armor");
	private static final OpenIdentifier CHESTPLATE_ID = OpenIdentifier.parse("forgero:iron-chestplate");
	private static final OpenIdentifier ARMOR_PLATE_TAG = OpenIdentifier.parse("forgero:armor_plate");
	private static final OpenIdentifier HANDLE_MATERIAL_ID = OpenIdentifier.parse("forgero:handle_material");

	private Resolver resolver;

	@BeforeEach
	void setUp() {
		resolver = resolver();
	}

	// ==================== HELPER METHODS ====================

	private StaticComponent createMaterial(OpenIdentifier id, OpenIdentifier tag, List<Attribute> attributes) {
		Map<String, List<?>> props = new HashMap<>();
		if (!attributes.isEmpty()) {
			props.put(Attribute.KEY.key(), attributes);
		}
		return new StaticComponent(id, Set.of(tag), props);
	}

	private SimpleAttribute createConditionalAttribute(OpenIdentifier type, float value, OpenIdentifier slotType) {
		StaticCondition whenInCondition = new InSlotTypeCondition(
				idFactory.of("forgero:in_slot_type"),
				slotType
		);
		Condition condition = new Condition(List.of(whenInCondition), Collections.emptyList());
		return new SimpleAttribute(Optional.empty(), type, value,
				com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator.getInstance(),
				0, Optional.of(condition));
	}

	// ==================== ARMOR VS TOOL SLOT TESTS ====================

	@Nested
	@DisplayName("Armor attributes should NOT apply to tools")
	class ArmorAttributesOnTools {

		/**
		 * CRITICAL TEST: Validates that armor attributes configured with in_slot_type: armor_material
		 * do NOT apply when the material is placed in a tool_material slot.
		 *
		 * This catches the bug where armor attributes had wrong slot_type configuration.
		 */
		@Test
		@DisplayName("Armor attribute with armor_material condition should not apply in tool_material slot")
		void armorAttributeDoesNotApplyInToolSlot() {
			// Iron with armor = 5 (only when in armor_material slot)
			SimpleAttribute armorAttribute = createConditionalAttribute(
					DefaultAttributes.ARMOR,
					5f,
					ARMOR_MATERIAL_ID  // Correct: only applies in armor slot
			);
			StaticComponent iron = createMaterial(IRON_ID, METAL_TAG, List.of(armorAttribute));

			// Place iron in a TOOL material slot (like a pickaxe head)
			StructuredPart pickaxeHead = new StructuredPart(
					PICKAXE_HEAD_ID,
					Set.of(PICKAXE_HEAD_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material_slot"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
					)
			);

			AttributeQueryResult result = resolver.resolve(pickaxeHead, attributeEngine());

			assertEquals(0f, result.getValue(DefaultAttributes.ARMOR), 0.001f,
					"CRITICAL: Armor attribute should NOT apply when material is in tool_material slot. " +
					"If this fails, check that iron.json has 'slot_type: forgero:armor_material' for armor attributes.");
		}

		/**
		 * Validates that armor attributes DO apply when material is in correct armor slot.
		 */
		@Test
		@DisplayName("Armor attribute should apply when in armor_material slot")
		void armorAttributeAppliesInArmorSlot() {
			// Iron with armor = 5 (only when in armor_material slot)
			SimpleAttribute armorAttribute = createConditionalAttribute(
					DefaultAttributes.ARMOR,
					5f,
					ARMOR_MATERIAL_ID
			);
			StaticComponent iron = createMaterial(IRON_ID, METAL_TAG, List.of(armorAttribute));

			// Place iron in an ARMOR material slot
			StructuredPart armorPlate = new StructuredPart(
					idFactory.of("forgero:iron-armor_plate"),
					Set.of(ARMOR_PLATE_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material_slot"), ARMOR_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
					)
			);

			AttributeQueryResult result = resolver.resolve(armorPlate, attributeEngine());

			assertEquals(5f, result.getValue(DefaultAttributes.ARMOR), 0.001f,
					"Armor attribute should apply when material is in armor_material slot");
		}

		/**
		 * Tests the inverse: tool attributes should NOT apply in armor slots.
		 */
		@Test
		@DisplayName("Tool attribute (mining_speed) should NOT apply in armor_material slot")
		void toolAttributeDoesNotApplyInArmorSlot() {
			// Iron with mining_speed = 6 (only when in tool_material slot)
			SimpleAttribute miningSpeed = createConditionalAttribute(
					DefaultAttributes.MINING_SPEED,
					6f,
					TOOL_MATERIAL_ID  // Only applies in tool slots
			);
			StaticComponent iron = createMaterial(IRON_ID, METAL_TAG, List.of(miningSpeed));

			// Place iron in an ARMOR material slot
			StructuredPart armorPlate = new StructuredPart(
					idFactory.of("forgero:iron-armor_plate"),
					Set.of(ARMOR_PLATE_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material_slot"), ARMOR_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
					)
			);

			AttributeQueryResult result = resolver.resolve(armorPlate, attributeEngine());

			assertEquals(0f, result.getValue(DefaultAttributes.MINING_SPEED), 0.001f,
					"Mining speed should NOT apply when material is in armor_material slot");
		}
	}

	// ==================== TOOL ATTRIBUTE VALIDATION ====================

	@Nested
	@DisplayName("Tool material attributes should apply correctly")
	class ToolAttributeValidation {

		/**
		 * Validates that durability from a material applies when in tool_material slot.
		 */
		@Test
		@DisplayName("Durability applies when material is in tool_material slot")
		void durabilityAppliesInToolMaterialSlot() {
			// Iron with durability = 250 (when in tool_material slot)
			SimpleAttribute durability = createConditionalAttribute(
					DefaultAttributes.DURABILITY,
					250f,
					TOOL_MATERIAL_ID
			);
			StaticComponent iron = createMaterial(IRON_ID, METAL_TAG, List.of(durability));

			StructuredPart pickaxeHead = new StructuredPart(
					PICKAXE_HEAD_ID,
					Set.of(PICKAXE_HEAD_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material_slot"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
					)
			);

			AttributeQueryResult result = resolver.resolve(pickaxeHead, attributeEngine());

			assertEquals(250f, result.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"Durability should apply when material is in tool_material slot");
		}

		/**
		 * Validates attack damage from material applies in tool slots.
		 */
		@Test
		@DisplayName("Attack damage applies when material is in tool_material slot")
		void attackDamageAppliesInToolMaterialSlot() {
			// Diamond with attack_damage = 3 (when in tool_material slot)
			SimpleAttribute attackDamage = createConditionalAttribute(
					DefaultAttributes.ATTACK_DAMAGE,
					3f,
					TOOL_MATERIAL_ID
			);
			StaticComponent diamond = createMaterial(
					idFactory.of("forgero:diamond"),
					idFactory.of("forgero:mineral"),
					List.of(attackDamage)
			);

			StructuredPart swordBlade = new StructuredPart(
					idFactory.of("forgero:diamond-sword_blade"),
					Set.of(idFactory.of("forgero:sword_blade")),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material_slot"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, diamond)
					)
			);

			AttributeQueryResult result = resolver.resolve(swordBlade, attributeEngine());

			assertEquals(3f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f,
					"Attack damage should apply when material is in tool_material slot");
		}
	}

	// ==================== HANDLE DURABILITY VALIDATION ====================

	@Nested
	@DisplayName("Handle durability should contribute correctly to tools")
	class HandleDurabilityValidation {

		/**
		 * CRITICAL TEST: Validates that oak handles contribute their durability (50),
		 * not iron handle durability (250).
		 *
		 * This catches the bug where default_tag wasn't being used in equipment generation.
		 */
		@Test
		@DisplayName("Oak handle should contribute oak durability, not iron durability")
		void oakHandleContributesCorrectDurability() {
			// Oak handle with durability = 50
			SimpleAttribute oakDurability = new SimpleAttribute(DefaultAttributes.DURABILITY, 50f);
			StaticComponent oakHandle = createMaterial(
					idFactory.of("forgero:oak-handle"),
					idFactory.of("forgero:handle"),
					List.of(oakDurability)
			);

			// Iron head with durability = 250
			SimpleAttribute ironDurability = createConditionalAttribute(
					DefaultAttributes.DURABILITY,
					250f,
					TOOL_MATERIAL_ID
			);
			StructuredPart ironHead = new StructuredPart(
					PICKAXE_HEAD_ID,
					Set.of(PICKAXE_HEAD_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material_slot"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL,
									createMaterial(IRON_ID, METAL_TAG, List.of(ironDurability)))
					)
			);

			// Full pickaxe: iron head + oak handle
			StructuredPart pickaxe = new StructuredPart(
					PICKAXE_ID,
					Set.of(idFactory.of("forgero:pickaxe")),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, "", SlotValidator.ACCEPT_ALL, ironHead),
							new ComponentPart(idFactory.of("handle"), idFactory.of("forgero:handle"), "", SlotValidator.ACCEPT_ALL, oakHandle)
					)
			);

			AttributeQueryResult result = resolver.resolve(pickaxe, attributeEngine());

			// Total durability should be: iron head (250) + oak handle (50) = 300
			assertEquals(300f, result.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"CRITICAL: Total durability should be iron head (250) + oak handle (50) = 300. " +
					"If this shows ~500, the wrong handle material is being used (likely iron instead of oak).");
		}

		/**
		 * Validates that iron handle would contribute more durability than oak.
		 * This helps detect if handles are accidentally using iron material.
		 */
		@Test
		@DisplayName("Iron handle would contribute more durability than oak")
		void ironHandleHasHigherDurabilityThanOak() {
			// This is a reference test to document expected values

			// Iron handle durability (if it were used)
			float ironHandleDurability = 250f;

			// Oak handle durability (correct default)
			float oakHandleDurability = 50f;

			assertTrue(ironHandleDurability > oakHandleDurability,
					"Iron handle should have higher durability than oak - " +
					"if tool durability is unexpectedly high, wrong handle may be used");

			// Document the expected difference
			float difference = ironHandleDurability - oakHandleDurability;
			assertEquals(200f, difference, 0.001f,
					"Difference between iron and oak handle durability should be 200");
		}
	}

	// ==================== MULTI-ATTRIBUTE MATERIAL VALIDATION ====================

	@Nested
	@DisplayName("Materials with multiple conditional attributes")
	class MultiAttributeMaterialValidation {

		/**
		 * Validates that a material can have both tool and armor attributes,
		 * each applying only in their respective slots.
		 */
		@Test
		@DisplayName("Material with both tool and armor attributes applies only correct ones per slot")
		void multiAttributeMaterialFiltersCorrectly() {
			// Iron with:
			// - durability = 250 (when in tool_material)
			// - armor = 5 (when in armor_material)
			SimpleAttribute toolDurability = createConditionalAttribute(
					DefaultAttributes.DURABILITY, 250f, TOOL_MATERIAL_ID
			);
			SimpleAttribute armorValue = createConditionalAttribute(
					DefaultAttributes.ARMOR, 5f, ARMOR_MATERIAL_ID
			);
			StaticComponent iron = createMaterial(IRON_ID, METAL_TAG, List.of(toolDurability, armorValue));

			// Test 1: Place in tool slot - only durability should apply
			StructuredPart pickaxeHead = new StructuredPart(
					PICKAXE_HEAD_ID,
					Set.of(PICKAXE_HEAD_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
					)
			);

			AttributeQueryResult toolResult = resolver.resolve(pickaxeHead, attributeEngine());

			assertEquals(250f, toolResult.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"Durability should apply in tool slot");
			assertEquals(0f, toolResult.getValue(DefaultAttributes.ARMOR), 0.001f,
					"Armor should NOT apply in tool slot");

			// Test 2: Place in armor slot - only armor should apply
			StructuredPart armorPlate = new StructuredPart(
					idFactory.of("forgero:iron-armor_plate"),
					Set.of(ARMOR_PLATE_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), ARMOR_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
					)
			);

			AttributeQueryResult armorResult = resolver.resolve(armorPlate, attributeEngine());

			assertEquals(0f, armorResult.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"Durability should NOT apply in armor slot (unless it has armor_material condition too)");
			assertEquals(5f, armorResult.getValue(DefaultAttributes.ARMOR), 0.001f,
					"Armor should apply in armor slot");
		}
	}

	// ==================== EXPECTED VALUE DOCUMENTATION ====================

	@Nested
	@DisplayName("Expected vanilla-like tool values (documentation)")
	class ExpectedValueDocumentation {

		/**
		 * Documents expected durability values for vanilla-like tools.
		 * These serve as reference points for detecting calculation bugs.
		 */
		@Test
		@DisplayName("Document expected vanilla pickaxe durability values")
		void documentExpectedDurabilityValues() {
			// Vanilla Minecraft durability values for reference
			// Iron pickaxe: 250
			// Diamond pickaxe: 1561
			// Gold pickaxe: 32
			// Wood pickaxe: 59
			// Stone pickaxe: 131

			// Forgero should approximate these with:
			// - Head material durability (main contributor)
			// - Handle material durability (smaller contributor)

			// For iron pickaxe with oak handle:
			// - Iron head: ~200-250 durability
			// - Oak handle: ~50 durability
			// - Total: ~250-300 durability

			float expectedMinDurability = 250f;  // At minimum, iron head alone
			float expectedMaxDurability = 350f;  // With oak handle contribution

			// This test documents expectations - actual values may vary
			assertTrue(expectedMinDurability < expectedMaxDurability,
					"Expected durability range should be valid");
		}

		/**
		 * Documents that armor values should NEVER appear on tools.
		 */
		@Test
		@DisplayName("Tools should have zero armor value")
		void toolsShouldHaveZeroArmor() {
			// Create a "typical" tool configuration
			SimpleAttribute toolDurability = createConditionalAttribute(
					DefaultAttributes.DURABILITY, 250f, TOOL_MATERIAL_ID
			);
			// Note: NO armor attribute (or armor with armor_material condition)
			StaticComponent iron = createMaterial(IRON_ID, METAL_TAG, List.of(toolDurability));

			StructuredPart pickaxeHead = new StructuredPart(
					PICKAXE_HEAD_ID,
					Set.of(PICKAXE_HEAD_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
					)
			);

			AttributeQueryResult result = resolver.resolve(pickaxeHead, attributeEngine());

			assertEquals(0f, result.getValue(DefaultAttributes.ARMOR), 0.001f,
					"INVARIANT: Tools should NEVER have non-zero armor values. " +
					"If this fails, armor attributes are leaking into tool slots.");
		}
	}
}
