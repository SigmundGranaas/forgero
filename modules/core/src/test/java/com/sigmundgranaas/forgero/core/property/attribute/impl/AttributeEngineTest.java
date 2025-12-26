package com.sigmundgranaas.forgero.core.property.attribute.impl;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
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
}
