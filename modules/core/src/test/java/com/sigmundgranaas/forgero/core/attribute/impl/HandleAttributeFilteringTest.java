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
import com.sigmundgranaas.forgero.core.condition.predicate.HasOtherContributorCondition;
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
 * Tests for attribute filtering on handles using HasOtherContributorCondition.
 *
 * <p>This test suite verifies that:</p>
 * <ul>
 *   <li>Handles do NOT show attack_damage when standalone (no other contributor)</li>
 *   <li>Handles DO show attack_damage when assembled into a tool with a weapon head</li>
 *   <li>Materials correctly use has_other_contributor to prevent attribute leaking</li>
 * </ul>
 *
 * <p>The iron.json material has attack_damage with two conditions:</p>
 * <ol>
 *   <li>in_slot_type: tool_material - must be in a tool_material slot</li>
 *   <li>has_other_contributor: attack_damage - another component must have attack_damage</li>
 * </ol>
 *
 * <p>For standalone handles, there's no other component with attack_damage, so the
 * condition should fail and attack_damage should NOT appear.</p>
 */
@DisplayName("Handle Attribute Filtering Tests")
class HandleAttributeFilteringTest extends ForgeroTest {

	private static final OpenIdentifier TOOL_MATERIAL_ID = OpenIdentifier.parse("forgero:tool_material");
	private static final OpenIdentifier HANDLE_TAG = OpenIdentifier.parse("forgero:handle");
	private static final OpenIdentifier SWORD_BLADE_TAG = OpenIdentifier.parse("forgero:sword_blade");

	private Resolver resolver;

	@BeforeEach
	void setUp() {
		resolver = resolver();
	}

	// ==================== HELPER METHODS ====================

	/**
	 * Creates an attribute with both in_slot_type AND has_other_contributor conditions.
	 * This mirrors the actual iron.json configuration.
	 */
	private SimpleAttribute createIronStyleAttribute(OpenIdentifier type, float value) {
		StaticCondition slotCondition = new InSlotTypeCondition(
				idFactory.of("forgero:in_slot_type"),
				TOOL_MATERIAL_ID
		);
		StaticCondition contributorCondition = new HasOtherContributorCondition(
				idFactory.of("forgero:has_other_contributor"),
				type
		);
		Condition condition = new Condition(List.of(slotCondition, contributorCondition), Collections.emptyList());
		return new SimpleAttribute(Optional.empty(), type, value,
				com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator.getInstance(),
				0, Optional.empty(), Optional.of(condition));
	}

	private StaticComponent createMaterial(OpenIdentifier id, OpenIdentifier tag, List<Attribute> attributes) {
		Map<String, List<?>> props = new HashMap<>();
		if (!attributes.isEmpty()) {
			props.put(Attribute.KEY.key(), attributes);
		}
		return new StaticComponent(id, Set.of(tag), props);
	}

	// ==================== STANDALONE HANDLE TESTS ====================

	@Nested
	@DisplayName("Standalone handles (not in a tool)")
	class StandaloneHandleTests {

		/**
		 * CRITICAL TEST: A standalone handle with iron material should NOT show attack_damage.
		 *
		 * The iron material has attack_damage with has_other_contributor condition.
		 * Since the handle template/schematic provides no attack_damage, there's no
		 * other contributor, so the condition should fail.
		 */
		@Test
		@DisplayName("Iron handle should NOT have attack_damage (no other contributor)")
		void ironHandleShouldNotHaveAttackDamage() {
			// Iron material with attack_damage that requires another contributor
			SimpleAttribute attackDamage = createIronStyleAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f);
			SimpleAttribute durability = createIronStyleAttribute(DefaultAttributes.DURABILITY, 250f);
			StaticComponent iron = createMaterial(IRON_ID, METAL_TAG, List.of(attackDamage, durability));

			// Handle with iron in tool_material slot
			// Note: Handle template only provides rarity (10), NO attack_damage
			StructuredPart ironHandle = new StructuredPart(
					idFactory.of("forgero:iron-handle"),
					Set.of(HANDLE_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
					)
			);

			AttributeQueryResult result = resolver.resolve(ironHandle, attributeEngine());

			assertEquals(0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f,
					"CRITICAL: Standalone iron handle should NOT have attack_damage because " +
					"there's no other component contributing attack_damage. " +
					"If this fails, check that has_other_contributor condition is working.");
		}

		/**
		 * Test that durability ALSO requires another contributor in iron.json style.
		 */
		@Test
		@DisplayName("Iron handle should NOT have durability (no other contributor in iron style)")
		void ironHandleShouldNotHaveDurabilityIfUsingIronStyle() {
			// Iron material with durability that requires another contributor
			SimpleAttribute durability = createIronStyleAttribute(DefaultAttributes.DURABILITY, 250f);
			StaticComponent iron = createMaterial(IRON_ID, METAL_TAG, List.of(durability));

			StructuredPart ironHandle = new StructuredPart(
					idFactory.of("forgero:iron-handle"),
					Set.of(HANDLE_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
					)
			);

			AttributeQueryResult result = resolver.resolve(ironHandle, attributeEngine());

			// With has_other_contributor condition, even durability requires another source
			assertEquals(0f, result.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"Iron durability with has_other_contributor should not apply on standalone handle");
		}

		/**
		 * Test that unconditional attributes DO apply to handles.
		 * This verifies the test infrastructure is working correctly.
		 */
		@Test
		@DisplayName("Unconditional material attributes DO apply to handles")
		void unconditionalAttributesApplyToHandles() {
			// Material with unconditional durability (no condition)
			SimpleAttribute durability = new SimpleAttribute(DefaultAttributes.DURABILITY, 50f);
			StaticComponent oak = createMaterial(
					idFactory.of("forgero:oak"),
					idFactory.of("forgero:wood"),
					List.of(durability)
			);

			StructuredPart oakHandle = new StructuredPart(
					idFactory.of("forgero:oak-handle"),
					Set.of(HANDLE_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, oak)
					)
			);

			AttributeQueryResult result = resolver.resolve(oakHandle, attributeEngine());

			assertEquals(50f, result.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"Unconditional attributes should apply to handles");
		}
	}

	// ==================== ASSEMBLED TOOL TESTS ====================

	@Nested
	@DisplayName("Handles assembled into tools")
	class AssembledToolTests {

		/**
		 * When iron material is in a sword blade (which provides base attack_damage),
		 * the has_other_contributor condition should PASS and iron's attack_damage should apply.
		 */
		@Test
		@DisplayName("Iron attack_damage DOES apply in sword blade with schematic base damage")
		void ironAttackDamageAppliesWithSchematicBaseDamage() {
			// Iron material with attack_damage requiring another contributor
			SimpleAttribute ironAttackDamage = createIronStyleAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f);
			StaticComponent iron = createMaterial(IRON_ID, METAL_TAG, List.of(ironAttackDamage));

			// Sword blade SHAPE provides base attack_damage (simulating schematic)
			SimpleAttribute schematicBaseDamage = new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 1f);

			// Create sword blade with both the schematic base and iron material
			Map<String, List<?>> bladeProps = new HashMap<>();
			bladeProps.put(Attribute.KEY.key(), List.of(schematicBaseDamage));

			StructuredPart swordBlade = new StructuredPart(
					idFactory.of("forgero:iron-sword_blade"),
					Set.of(SWORD_BLADE_TAG),
					bladeProps,
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
					)
			);

			AttributeQueryResult result = resolver.resolve(swordBlade, attributeEngine());

			// Total attack_damage should be: schematic base (1) + iron (4) = 5
			assertEquals(5f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f,
					"Iron attack_damage should apply when sword blade schematic provides base damage. " +
					"Expected: schematic(1) + iron(4) = 5");
		}

		/**
		 * When assembled into a full tool with multiple parts contributing durability,
		 * has_other_contributor should pass for all parts.
		 */
		@Test
		@DisplayName("Full tool with multiple durability contributors combines correctly")
		void fullToolCombinesDurabilityFromMultipleParts() {
			// Iron head material with conditional durability
			SimpleAttribute ironDurability = createIronStyleAttribute(DefaultAttributes.DURABILITY, 250f);
			StaticComponent ironMaterial = createMaterial(IRON_ID, METAL_TAG, List.of(ironDurability));

			// Oak handle material with conditional durability
			SimpleAttribute oakDurability = createIronStyleAttribute(DefaultAttributes.DURABILITY, 50f);
			StaticComponent oakMaterial = createMaterial(
					idFactory.of("forgero:oak"),
					idFactory.of("forgero:wood"),
					List.of(oakDurability)
			);

			// Create pickaxe head with iron
			StructuredPart pickaxeHead = new StructuredPart(
					PICKAXE_HEAD_ID,
					Set.of(PICKAXE_HEAD_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("head_material"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, ironMaterial)
					)
			);

			// Create handle with oak
			StructuredPart handle = new StructuredPart(
					idFactory.of("forgero:oak-handle"),
					Set.of(HANDLE_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("handle_material"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, oakMaterial)
					)
			);

			// Assemble full pickaxe
			StructuredPart pickaxe = new StructuredPart(
					PICKAXE_ID,
					Set.of(idFactory.of("forgero:pickaxe")),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, "", SlotValidator.ACCEPT_ALL, pickaxeHead),
							new ComponentPart(idFactory.of("handle"), HANDLE_TAG, "", SlotValidator.ACCEPT_ALL, handle)
					)
			);

			AttributeQueryResult result = resolver.resolve(pickaxe, attributeEngine());

			// Both materials contribute durability because they are "other contributors" to each other
			// Total: iron (250) + oak (50) = 300
			assertEquals(300f, result.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"Full tool should combine durability from iron head (250) and oak handle (50). " +
					"has_other_contributor passes because each material sees the other as a contributor.");
		}
	}

	// ==================== CONDITION INTERACTION TESTS ====================

	@Nested
	@DisplayName("Condition interaction tests")
	class ConditionInteractionTests {

		/**
		 * Tests that in_slot_type and has_other_contributor work together correctly.
		 * Both conditions must pass for the attribute to apply.
		 */
		@Test
		@DisplayName("Both in_slot_type AND has_other_contributor must pass")
		void bothConditionsMustPass() {
			// Iron with attack_damage requiring: tool_material slot AND another contributor
			SimpleAttribute attackDamage = createIronStyleAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f);
			StaticComponent iron = createMaterial(IRON_ID, METAL_TAG, List.of(attackDamage));

			// Place iron in a tool_material slot, but with NO other attack_damage contributor
			StructuredPart handle = new StructuredPart(
					idFactory.of("forgero:iron-handle"),
					Set.of(HANDLE_TAG),
					new HashMap<>(), // No attributes on the handle itself
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
					)
			);

			AttributeQueryResult result = resolver.resolve(handle, attributeEngine());

			// in_slot_type PASSES (iron is in tool_material slot)
			// has_other_contributor FAILS (no other component has attack_damage)
			// Therefore: attack_damage should NOT apply
			assertEquals(0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f,
					"Attack damage should NOT apply because has_other_contributor condition fails, " +
					"even though in_slot_type condition passes.");
		}

		/**
		 * Verifies that different attribute types have independent has_other_contributor checks.
		 */
		@Test
		@DisplayName("Different attribute types have independent contributor checks")
		void independentContributorChecksPerAttributeType() {
			// Iron with attack_damage and durability, both requiring other contributors
			SimpleAttribute attackDamage = createIronStyleAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f);
			SimpleAttribute durability = createIronStyleAttribute(DefaultAttributes.DURABILITY, 250f);
			StaticComponent iron = createMaterial(IRON_ID, METAL_TAG, List.of(attackDamage, durability));

			// Create a part that provides base durability but NOT attack_damage
			SimpleAttribute baseDurability = new SimpleAttribute(DefaultAttributes.DURABILITY, 10f);
			Map<String, List<?>> partProps = new HashMap<>();
			partProps.put(Attribute.KEY.key(), List.of(baseDurability));

			StructuredPart part = new StructuredPart(
					idFactory.of("forgero:test-part"),
					Set.of(idFactory.of("forgero:test")),
					partProps,
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
					)
			);

			AttributeQueryResult result = resolver.resolve(part, attributeEngine());

			// Durability should apply: base(10) + iron(250) = 260
			// (Part provides base durability, satisfying has_other_contributor for iron's durability)
			assertEquals(260f, result.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"Durability should apply because part provides base durability");

			// Attack damage should NOT apply (no other source of attack_damage)
			assertEquals(0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f,
					"Attack damage should NOT apply because there's no other attack_damage contributor");
		}
	}
}
