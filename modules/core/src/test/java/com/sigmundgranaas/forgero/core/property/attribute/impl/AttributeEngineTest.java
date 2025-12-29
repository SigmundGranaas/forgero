package com.sigmundgranaas.forgero.core.property.attribute.impl;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.testutils.TestIdentifiers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeEngineTest extends ForgeroTest {
	private Resolver resolver;

	@BeforeEach
	void setUp() {
		resolver = resolver();
	}

	/**
	 * Test case 1: Non-Structured Component (Default Strategy)
	 * Simple attributes should be aggregated correctly.
	 */
	@Test
	void testStaticEquipmentBakesSimpleAttributesCorrectly() {
		Component basicAxe = tool("basic_axe")
				.withTag("axe")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 10f)
				.withAttribute(DefaultAttributes.ATTACK_SPEED, 1.2f)
				.build();

		AttributeQueryResult result = resolver.resolve(basicAxe, attributeEngine());

		assertEquals(10f, result.getValue(DefaultAttributes.ATTACK_DAMAGE));
		assertEquals(1.2f, result.getValue(DefaultAttributes.ATTACK_SPEED));
	}

	/**
	 * Test case 2: Structured Component with Simple Attributes
	 * Attributes from different parts should be aggregated.
	 */
	@Test
	void testStructuredEquipmentWithOnlySimpleAttributes() {
		Component head = part(PICKAXE_HEAD_ID)
				.withTag(PICKAXE_HEAD_TAG)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f)
				.build();

		Component handle = part(HANDLE_ID)
				.withTag(HANDLE_TAG)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f)
				.build();

		Component pickaxe = tool(PICKAXE_ID)
				.withTag("pickaxe")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 3f)
				.withPart(head, HEAD_SLOT_ID.toString(), PICKAXE_HEAD_TAG)
				.withPart(handle, HANDLE_SLOT_ID.toString(), HANDLE_TAG)
				.build();

		AttributeQueryResult result = resolver.resolve(pickaxe, attributeEngine());
		assertEquals(10f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), "Total attack damage should be base + head + handle (3+5+2=10)");
	}

	/**
	 * Test case 3: Multiple Attribute Types
	 * Different attribute types should be aggregated independently.
	 */
	@Test
	void testMixedAttributeTypes() {
		Component head = part(PICKAXE_HEAD_ID)
				.withTag(PICKAXE_HEAD_TAG)
				.withAttribute(DefaultAttributes.MINING_SPEED, 3.0f)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 5.0f)
				.build();

		Component handle = part(HANDLE_ID)
				.withTag(HANDLE_TAG)
				.build();

		Component tool = tool(PICKAXE_ID)
				.withTag("tool")
				.withAttribute(DefaultAttributes.MINING_SPEED, 2.0f)
				.withPart(head, HEAD_SLOT_ID.toString(), PICKAXE_HEAD_TAG)
				.withPart(handle, HANDLE_SLOT_ID.toString(), HANDLE_TAG)
				.build();

		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		assertEquals(5.0f, result.getValue(DefaultAttributes.MINING_SPEED), "Mining Speed should be base + head (2.0 + 3.0 = 5.0)");
		assertEquals(5.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), "Attack Damage should be from head only (5.0)");
	}

	// ==================== Edge Case Tests ====================

	/**
	 * Edge case: Component with many attributes (100+).
	 * Tests that the engine can handle large numbers of attributes efficiently.
	 */
	@Test
	void testComponentWithManyAttributes() {
		var builder = tool("attribute_heavy_tool")
				.withTag("tool");

		// Add 100 attack damage attributes
		for (int i = 0; i < 100; i++) {
			builder.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 1.0f);
		}

		Component tool = builder.build();
		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		// All 100 attributes should be aggregated
		assertEquals(100.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE),
				"Should aggregate 100 attack damage attributes");
	}

	/**
	 * Edge case: Attribute with Float.MAX_VALUE.
	 * Tests handling of extreme float values.
	 */
	@Test
	void testAttributeWithMaxValue() {
		Component tool = tool("max_damage_tool")
				.withTag("tool")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, Float.MAX_VALUE)
				.build();

		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		assertEquals(Float.MAX_VALUE, result.getValue(DefaultAttributes.ATTACK_DAMAGE),
				"Should handle Float.MAX_VALUE correctly");
	}

	/**
	 * Edge case: Attribute with Float.MIN_VALUE (smallest positive value).
	 * Tests handling of very small positive values.
	 */
	@Test
	void testAttributeWithMinValue() {
		Component tool = tool("min_damage_tool")
				.withTag("tool")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, Float.MIN_VALUE)
				.build();

		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		assertEquals(Float.MIN_VALUE, result.getValue(DefaultAttributes.ATTACK_DAMAGE),
				"Should handle Float.MIN_VALUE correctly");
	}

	/**
	 * Edge case: Zero attribute values.
	 * Tests that zero values are handled correctly.
	 */
	@Test
	void testZeroAttributeValue() {
		Component tool = tool("zero_damage_tool")
				.withTag("tool")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 0.0f)
				.build();

		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		assertEquals(0.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE),
				"Zero attribute value should be preserved");
	}

	/**
	 * Edge case: Negative attribute values.
	 * Tests that negative values are handled correctly (may represent debuffs).
	 */
	@Test
	void testNegativeAttributeValue() {
		Component tool = tool("negative_damage_tool")
				.withTag("tool")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, -5.0f)
				.build();

		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		assertEquals(-5.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE),
				"Negative attribute value should be preserved");
	}

	/**
	 * Edge case: Aggregation resulting in overflow.
	 * Tests behavior when adding Float.MAX_VALUE multiple times.
	 */
	@Test
	void testAttributeAggregationOverflow() {
		Component part1 = part("part1")
				.withTag("part")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, Float.MAX_VALUE)
				.build();

		Component part2 = part("part2")
				.withTag("part")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, Float.MAX_VALUE)
				.build();

		Component tool = tool("overflow_tool")
				.withTag("tool")
				.withPart(part1, "slot1", TestIdentifiers.id("part"))
				.withPart(part2, "slot2", TestIdentifiers.id("part"))
				.build();

		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		// Float.MAX_VALUE + Float.MAX_VALUE = Infinity in Java
		float aggregated = result.getValue(DefaultAttributes.ATTACK_DAMAGE);
		assertEquals(Float.POSITIVE_INFINITY, aggregated,
				"Overflow should result in POSITIVE_INFINITY");
	}

	/**
	 * Edge case: Mixing positive and negative extreme values.
	 * Tests that positive and negative extremes cancel correctly.
	 */
	@Test
	void testMixedExtremeValues() {
		Component positivePart = part("positive_part")
				.withTag("part")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, Float.MAX_VALUE)
				.build();

		Component negativePart = part("negative_part")
				.withTag("part")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, -Float.MAX_VALUE)
				.build();

		Component tool = tool("mixed_extreme_tool")
				.withTag("tool")
				.withPart(positivePart, "slot1", TestIdentifiers.id("part"))
				.withPart(negativePart, "slot2", TestIdentifiers.id("part"))
				.build();

		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		// MAX_VALUE + (-MAX_VALUE) should be 0, but floating point may have precision issues
		float aggregated = result.getValue(DefaultAttributes.ATTACK_DAMAGE);
		assertEquals(0.0f, aggregated, 0.01f,
				"Positive and negative MAX_VALUE should cancel to approximately 0");
	}

	/**
	 * Edge case: Deeply nested component structure.
	 * Tests attribute aggregation through many levels of nesting.
	 */
	@Test
	void testDeeplyNestedComponents() {
		Component current = part("deep_part_0")
				.withTag("part")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 1.0f)
				.build();

		// Create a chain of 20 nested components
		for (int i = 1; i < 20; i++) {
			var slotType = TestIdentifiers.id("part");
			current = part("deep_part_" + i)
					.withTag("part")
					.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 1.0f)
					.withStructureSlot(new ComponentPart(
							TestIdentifiers.id("nested_slot"),
							slotType,
							"",
							SlotValidator.requireTag(slotType),
							current))
					.build();
		}

		Component tool = tool("deep_tool")
				.withTag("tool")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 1.0f)
				.withPart(current, "root_slot", TestIdentifiers.id("part"))
				.build();

		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		// Should aggregate all 21 attributes (20 parts + 1 tool)
		assertEquals(21.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE),
				"Should aggregate attributes through 20 levels of nesting");
	}

	/**
	 * Edge case: Component with many parts at same level.
	 * Tests wide component structure (many siblings).
	 */
	@Test
	void testComponentWithManyParts() {
		var builder = tool("many_parts_tool")
				.withTag("tool")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 1.0f);

		// Add 50 parts, each contributing 1.0 damage
		for (int i = 0; i < 50; i++) {
			Component part = part("part_" + i)
					.withTag("part")
					.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 1.0f)
					.build();
			builder.withPart(part, "slot_" + i, TestIdentifiers.id("part"));
		}

		Component tool = builder.build();
		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		// Should aggregate 51 attributes (50 parts + 1 tool)
		assertEquals(51.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE),
				"Should aggregate attributes from 50 parts");
	}

	/**
	 * Edge case: Attribute aggregation with very small increments.
	 * Tests floating point precision with many small additions.
	 */
	@Test
	void testSmallIncrementAggregation() {
		var builder = tool("small_increment_tool")
				.withTag("tool");

		// Add 1000 parts, each contributing 0.001 damage
		for (int i = 0; i < 1000; i++) {
			Component part = part("small_part_" + i)
					.withTag("part")
					.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 0.001f)
					.build();
			builder.withPart(part, "slot_" + i, TestIdentifiers.id("part"));
		}

		Component tool = builder.build();
		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		// Should aggregate to approximately 1.0 (1000 * 0.001)
		// Use delta for floating point comparison
		assertEquals(1.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.01f,
				"Should aggregate 1000 small increments to approximately 1.0");
	}

	/**
	 * Edge case: Empty component (no attributes).
	 * Tests that components without attributes don't cause errors.
	 */
	@Test
	void testComponentWithNoAttributes() {
		Component tool = tool("empty_tool")
				.withTag("tool")
				.build();

		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		// Querying non-existent attribute should return 0 or default value
		float damage = result.getValue(DefaultAttributes.ATTACK_DAMAGE);
		assertEquals(0.0f, damage, "Component with no attributes should return 0");
	}

	/**
	 * Edge case: Component with duplicate attribute keys.
	 * Tests that multiple attributes of same type are aggregated correctly.
	 */
	@Test
	void testDuplicateAttributeKeys() {
		Component tool = tool("duplicate_attr_tool")
				.withTag("tool")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 5.0f)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 3.0f)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 2.0f)
				.build();

		AttributeQueryResult result = resolver.resolve(tool, attributeEngine());

		// All three attributes should be aggregated
		assertEquals(10.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE),
				"Duplicate attribute keys should be aggregated");
	}
}
