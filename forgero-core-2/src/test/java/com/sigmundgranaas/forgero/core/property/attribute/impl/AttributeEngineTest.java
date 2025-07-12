package com.sigmundgranaas.forgero.core.property.attribute.impl;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttributeComponent;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.SubtractionOperator;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.component.impl.StaticEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StructuredEquipment;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributeEngineTest extends ForgeroTest {
	private Resolver resolver;

	private static final OpenIdentifier SPECIAL_COMPOSITE_ATTRIBUTE_TYPE = idFactory.of("forgero", "special_composite_attribute");
	private static final OpenIdentifier LOCAL_COMPOSITE_KEY_A = idFactory.of("forgero", "local_key_a");
	private static final OpenIdentifier LOCAL_COMPOSITE_KEY_B = idFactory.of("forgero", "local_key_b");


	@BeforeEach
	void setUp() {
		resolver = new ResolverEngine();
	}

	// Helper method to flatten component hierarchy for bake tests
	private List<Component> flattenComponentTree(Component root) {
		List<Component> all = new ArrayList<>();
		collectChildren(root, all);
		return all;
	}

	private void collectChildren(Component component, List<Component> collector) {
		collector.add(component);
		for (Component child : component.getChildren()) {
			collectChildren(child, collector);
		}
	}

	/**
	 * Test case 1: Non-Structured Component (Default Strategy)
	 * Should behave exactly as before, no composite handling special logic applies.
	 */
	@Test
	void testStaticEquipmentBakesSimpleAttributesCorrectly() {
		StaticEquipment basicAxe = new StaticEquipment(
				idFactory.of("basic_axe"),
				Set.of(idFactory.of("axe")),
				List.of(
						new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 10f),
						new SimpleAttribute(DefaultAttributes.ATTACK_SPEED, 1.2f)
				)
		);

		AttributeQueryResult result = resolver.resolve(basicAxe, AttributeEngine.KEY).orElseThrow();

		assertEquals(10f, result.getValue(DefaultAttributes.ATTACK_DAMAGE));
		assertEquals(1.2f, result.getValue(DefaultAttributes.ATTACK_SPEED));
	}

	/**
	 * Test case 2: Structured Component with only Simple Attributes
	 * Uses CompositeBakingStrategy, but no CompositeAttributeComponents are present.
	 * Should still correctly aggregate simple attributes.
	 */
	@Test
	void testStructuredEquipmentWithOnlySimpleAttributes() {
		StaticComponent head = part(PICKAXE_HEAD_ID, PICKAXE_HEAD_TAG, List.of(new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f)));
		StaticComponent handle = part(HANDLE_ID, HANDLE_TAG, List.of(new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f)));

		StructuredEquipment pickaxe = new StructuredEquipment(
				PICKAXE_ID,
				Set.of(idFactory.of("pickaxe")),
				List.of(new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 3f)), // Base attribute
				new ComponentStructure(slotsMap( // Update to Map.of
						slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head),
						slot(HANDLE_SLOT_ID, HANDLE_TAG, handle)
				))
		);

		AttributeQueryResult result = resolver.resolve(pickaxe, AttributeEngine.KEY).orElseThrow();
		assertEquals(10f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), "Total attack damage should be base + head + handle (3+5+2=10)");
	}

	/**
	 * Test case 3: Structured Component with Successful Composite Attributes (LOCAL to a single component)
	 * Verifies that CompositeAttributeComponents are grouped and converted into a single CompositeAttribute
	 * ONLY if they belong to the same component and share the same composite key.
	 */
	@Test
	void testStructuredEquipmentWithSuccessfulLocalCompositeAttributes() {
		// These components are all on the 'head' and will form a single composite attribute.
		CompositeAttributeComponent headCompAdd = new CompositeAttributeComponent(DefaultAttributes.ATTACK_DAMAGE, 10f, AdditionOperator.getInstance(), 0, LOCAL_COMPOSITE_KEY_A);
		CompositeAttributeComponent headCompMul = new CompositeAttributeComponent(DefaultAttributes.ATTACK_DAMAGE, 2f, MultiplicationOperator.getInstance(), 1, LOCAL_COMPOSITE_KEY_A);
		CompositeAttributeComponent headCompSub = new CompositeAttributeComponent(DefaultAttributes.ATTACK_DAMAGE, 5f, SubtractionOperator.getInstance(), 2, LOCAL_COMPOSITE_KEY_A);

		StaticComponent head = part(PICKAXE_HEAD_ID, PICKAXE_HEAD_TAG, List.of(headCompAdd, headCompMul, headCompSub));
		StaticComponent handle = part(HANDLE_ID, HANDLE_TAG, Collections.emptyList()); // No attributes from handle

		// Base attribute on the tool itself
		SimpleAttribute baseDamage = new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 100f);

		StructuredEquipment pickaxe = new StructuredEquipment(
				PICKAXE_ID,
				Set.of(idFactory.of("pickaxe")),
				List.of(baseDamage),
				new ComponentStructure(slotsMap( // Update to Map.of
						slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head),
						slot(HANDLE_SLOT_ID, HANDLE_TAG, handle)
				))
		);

		// Expected composite calculation (internal to CompositeAttribute formed from head's properties):
		// (0 + 10) * 2 - 5 = 20 - 5 = 15
		// Total ATTACK_DAMAGE: Base (100) + Composite from head (15) = 115
		AttributeQueryResult result = resolver.resolve(pickaxe, AttributeEngine.KEY).orElseThrow();

		assertEquals(115f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), "Total ATTACK_DAMAGE should be sum of base and composite attribute from head.");
	}

	/**
	 * Test case 3b: Composite attributes from DIFFERENT components with same type/key should NOT combine,
	 * and if they cannot form a composite individually, they should be DISCARDED.
	 */
	@Test
	void testCompositeAttributesFromDifferentComponentsAreDiscardedIfNoLocalComposition() {
		// Head contributes a CompositeAttributeComponent
		CompositeAttributeComponent headCompAdd = new CompositeAttributeComponent(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE, 10f, AdditionOperator.getInstance(), 0, LOCAL_COMPOSITE_KEY_A);

		// Handle contributes another CompositeAttributeComponent, but it's on a different component
		CompositeAttributeComponent handleCompMul = new CompositeAttributeComponent(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE, 2f, MultiplicationOperator.getInstance(), 1, LOCAL_COMPOSITE_KEY_A);

		StaticComponent head = part(PICKAXE_HEAD_ID, PICKAXE_HEAD_TAG, List.of(headCompAdd));
		StaticComponent handle = part(HANDLE_ID, HANDLE_TAG, List.of(handleCompMul));

		StructuredEquipment pickaxe = new StructuredEquipment(
				PICKAXE_ID,
				Set.of(idFactory.of("pickaxe")),
				Collections.emptyList(), // No base attributes to keep test focused
				new ComponentStructure(slotsMap( // Update to Map.of
						slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head),
						slot(HANDLE_SLOT_ID, HANDLE_TAG, handle)
				))
		);

		AttributeQueryResult result = resolver.resolve(pickaxe, AttributeEngine.KEY).orElseThrow();

		// Expected:
		// - Head: `headCompAdd` (single operator, fails to form composite) -> DISCARDED
		// - Handle: `handleCompMul` (single operator, fails to form composite) -> DISCARDED
		// Therefore, the total for SPECIAL_COMPOSITE_ATTRIBUTE_TYPE should be 0.
		assertEquals(0f, result.getValue(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE), "Composite components from different parts should be discarded if they don't form a local composite.");
	}


	/**
	 * Test case 4: Structured Component with Composite Attributes (Failing Local Composition)
	 * Ensures that CompositeAttributeComponents that cannot form a valid CompositeAttribute
	 * (e.g., only one operator type within their component's group) are DISCARDED.
	 */
	@Test
	void testStructuredEquipmentWithFailingLocalCompositeAttributesAreDiscarded() {
		// These components are all on the 'head' but only contain Addition operators,
		// so they cannot form a CompositeAttribute. They should be DISCARDED.
		CompositeAttributeComponent headCompAdd1 = new CompositeAttributeComponent(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE, 10f, AdditionOperator.getInstance(), 0, LOCAL_COMPOSITE_KEY_A);
		CompositeAttributeComponent headCompAdd2 = new CompositeAttributeComponent(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE, 5f, AdditionOperator.getInstance(), 0, LOCAL_COMPOSITE_KEY_A);

		StaticComponent head = part(PICKAXE_HEAD_ID, PICKAXE_HEAD_TAG, List.of(headCompAdd1, headCompAdd2));
		StaticComponent handle = part(HANDLE_ID, HANDLE_TAG, Collections.emptyList()); // No attributes from handle

		StructuredEquipment pickaxe = new StructuredEquipment(
				PICKAXE_ID,
				Set.of(idFactory.of("pickaxe")),
				Collections.emptyList(),
				new ComponentStructure(slotsMap( // Update to Map.of
						slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head),
						slot(HANDLE_SLOT_ID, HANDLE_TAG, handle)
				))
		);

		AttributeQueryResult result = resolver.resolve(pickaxe, AttributeEngine.KEY).orElseThrow();

		// Expected: Since composition fails for the head's composite components (only addition operators),
		// they should be discarded. Result for SPECIAL_COMPOSITE_ATTRIBUTE_TYPE should be 0.
		assertEquals(0f, result.getValue(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE), "Failing composite components should be discarded.");
	}

	/**
	 * Test case 5: Mixed Components and Attribute Combinations
	 * Tests a combination of SimpleAttributes, successful local composite attributes, and
	 * `CompositeAttributeComponent`s that are discarded due to failing local composition.
	 */
	@Test
	void testMixedAttributeCombinationsWithDiscardingFailedComposites() {
		// Base tool attribute
		SimpleAttribute baseMiningSpeed = new SimpleAttribute(DefaultAttributes.MINING_SPEED, 2.0f); // MS: 2.0

		// Head attributes:
		// - Successful composite for SPECIAL_COMPOSITE_ATTRIBUTE_TYPE
		CompositeAttributeComponent headCompA1 = new CompositeAttributeComponent(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE, 5f, AdditionOperator.getInstance(), 0, LOCAL_COMPOSITE_KEY_A);
		CompositeAttributeComponent headCompA2 = new CompositeAttributeComponent(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE, 1.2f, MultiplicationOperator.getInstance(), 1, LOCAL_COMPOSITE_KEY_A);
		// Head Calc A (internal composite): (0 + 5) * 1.2 = 6.0

		// Handle attributes:
		// - Failing composite for DefaultAttributes.ATTACK_DAMAGE (only Addition operators), these should be DISCARDED
		CompositeAttributeComponent handleCompB1 = new CompositeAttributeComponent(DefaultAttributes.ATTACK_DAMAGE, 3f, AdditionOperator.getInstance(), 0, LOCAL_COMPOSITE_KEY_B);
		CompositeAttributeComponent handleCompB2 = new CompositeAttributeComponent(DefaultAttributes.ATTACK_DAMAGE, 7f, AdditionOperator.getInstance(), 0, LOCAL_COMPOSITE_KEY_B);

		// - Simple attribute on handle
		SimpleAttribute handleMiningSpeed = new SimpleAttribute(DefaultAttributes.MINING_SPEED, 3.0f); // MS: 3.0

		// Parts
		StaticComponent head = part(PICKAXE_HEAD_ID, PICKAXE_HEAD_TAG, List.of(headCompA1, headCompA2));
		StaticComponent handle = part(HANDLE_ID, HANDLE_TAG, List.of(handleCompB1, handleCompB2, handleMiningSpeed));

		StructuredEquipment tool = new StructuredEquipment(
				PICKAXE_ID,
				Set.of(idFactory.of("tool")),
				List.of(baseMiningSpeed),
				new ComponentStructure(slotsMap( // Update to Map.of
						slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head),
						slot(HANDLE_SLOT_ID, HANDLE_TAG, handle)
				))
		);

		AttributeQueryResult result = resolver.resolve(tool, AttributeEngine.KEY).orElseThrow();

		// Verify Mining Speed: Base (2.0) + Handle (3.0) = 5.0
		assertEquals(5.0f, result.getValue(DefaultAttributes.MINING_SPEED), "Mining Speed should be sum of base and handle.");

		// Verify SPECIAL_COMPOSITE_ATTRIBUTE_TYPE (successful composite from head)
		assertEquals(6.0f, result.getValue(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE), "Special composite attribute should be calculated as (5 * 1.2).");

		// Verify ATTACK_DAMAGE (failing composite from handle, these should be DISCARDED, so value is 0)
		assertEquals(0.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), "Attack Damage components should be discarded as they fail to form a composite.");
	}

	/**
	 * Test to ensure that CompositeAttributeComponents are correctly filtered out
	 * from the final baked list if they successfully form a CompositeAttribute.
	 * Only the CompositeAttribute should remain.
	 */
	@Test
	void testSuccessfulCompositeComponentsAreReplacedByCompositeAttribute() {
		CompositeAttributeComponent compAdd = new CompositeAttributeComponent(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE, 10f, AdditionOperator.getInstance(), 0, LOCAL_COMPOSITE_KEY_A);
		CompositeAttributeComponent compMul = new CompositeAttributeComponent(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE, 2f, MultiplicationOperator.getInstance(), 1, LOCAL_COMPOSITE_KEY_A);

		StaticComponent head = part(PICKAXE_HEAD_ID, PICKAXE_HEAD_TAG, List.of(compAdd, compMul));

		StructuredEquipment tool = new StructuredEquipment(
				PICKAXE_ID,
				Set.of(idFactory.of("tool")),
				Collections.emptyList(),
				new ComponentStructure(slotsMap( // Update to Map.of
						slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head)
				))
		);

		AttributeEngine engine = new AttributeEngine();
		// Use the new helper to flatten the component tree for bake method
		List<com.sigmundgranaas.forgero.core.attribute.api.Attribute> bakedAttributes = engine.bake(flattenComponentTree(tool).stream());

		// We expect one CompositeAttribute to be present, and no individual CompositeAttributeComponents
		long compositeAttributeCount = bakedAttributes.stream()
				.filter(attr -> attr instanceof com.sigmundgranaas.forgero.core.attribute.api.CompositeAttribute)
				.count();
		assertEquals(1, compositeAttributeCount, "One CompositeAttribute should be formed.");

		long compositeComponentCount = bakedAttributes.stream()
				.filter(attr -> attr instanceof CompositeAttributeComponent)
				.count();
		assertEquals(0, compositeComponentCount, "No individual CompositeAttributeComponents should remain after baking.");

		// Verify the value still
		AttributeQueryResult result = resolver.resolve(tool, AttributeEngine.KEY).orElseThrow();
		assertEquals(20f, result.getValue(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE), "Composite attribute value should be 20 (0 + 10 * 2).");
	}

	/**
	 * Test to ensure that if a CompositeAttribute fails to form, its constituent
	 * CompositeAttributeComponents are DISCARDED.
	 */
	@Test
	void testFailingCompositeComponentsAreDiscarded() {
		CompositeAttributeComponent compAdd1 = new CompositeAttributeComponent(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE, 10f, AdditionOperator.getInstance(), 0, LOCAL_COMPOSITE_KEY_A);
		CompositeAttributeComponent compAdd2 = new CompositeAttributeComponent(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE, 5f, AdditionOperator.getInstance(), 0, LOCAL_COMPOSITE_KEY_A); // Fails due to single operator type

		StaticComponent head = part(PICKAXE_HEAD_ID, PICKAXE_HEAD_TAG, List.of(compAdd1, compAdd2));

		StructuredEquipment tool = new StructuredEquipment(
				PICKAXE_ID,
				Set.of(idFactory.of("tool")),
				Collections.emptyList(),
				new ComponentStructure(slotsMap( // Update to Map.of
						slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head)
				))
		);

		AttributeEngine engine = new AttributeEngine();
		// Use the helper to flatten the component tree for bake method.
		List<com.sigmundgranaas.forgero.core.attribute.api.Attribute> bakedAttributes = engine.bake(flattenComponentTree(tool).stream());

		// We expect no CompositeAttribute, and no SimpleAttributes derived from these (they are discarded)
		long compositeAttributeCount = bakedAttributes.stream()
				.filter(attr -> attr instanceof com.sigmundgranaas.forgero.core.attribute.api.CompositeAttribute)
				.count();
		assertEquals(0, compositeAttributeCount, "No CompositeAttribute should be formed.");

		long simpleAttributeCount = bakedAttributes.stream()
				.filter(attr -> attr instanceof SimpleAttribute)
				.filter(attr -> attr.type().equals(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE))
				.count();
		assertEquals(0, simpleAttributeCount, "No SimpleAttributes should be present from discarded composite components.");

		long compositeComponentCount = bakedAttributes.stream()
				.filter(attr -> attr instanceof CompositeAttributeComponent)
				.count();
		assertEquals(0, compositeComponentCount, "No individual CompositeAttributeComponents should remain after baking.");

		// Verify the value still (should be 0 as components are discarded)
		AttributeQueryResult result = resolver.resolve(tool, AttributeEngine.KEY).orElseThrow();
		assertEquals(0f, result.getValue(SPECIAL_COMPOSITE_ATTRIBUTE_TYPE), "Composite attribute value should be 0 as components are discarded.");
	}
}
