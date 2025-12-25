package com.sigmundgranaas.forgero.core.property.attribute.impl;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.component.impl.StaticEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StructuredEquipment;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeEngineTest extends ForgeroTest {
	private Resolver resolver;

	@BeforeEach
	void setUp() {
		resolver = new ResolverEngine();
	}

	/**
	 * Helper method to create StaticComponent with attributes.
	 */
	private StaticComponent part(OpenIdentifier id, OpenIdentifier tag, List<Attribute> attributes) {
		Map<String, List<?>> properties = new HashMap<>();
		if (attributes != null && !attributes.isEmpty()) {
			properties.put(Attribute.KEY.key(), attributes);
		}
		return new StaticComponent(id, Set.of(tag), properties);
	}

	/**
	 * Test case 1: Non-Structured Component (Default Strategy)
	 * Simple attributes should be aggregated correctly.
	 */
	@Test
	void testStaticEquipmentBakesSimpleAttributesCorrectly() {
		List<Attribute> attributes = List.of(
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 10f),
				new SimpleAttribute(DefaultAttributes.ATTACK_SPEED, 1.2f)
		);
		Map<String, List<?>> properties = new HashMap<>();
		properties.put(Attribute.KEY.key(), attributes);

		StaticEquipment basicAxe = new StaticEquipment(
				idFactory.of("basic_axe"),
				Set.of(idFactory.of("axe")),
				properties
		);

		AttributeQueryResult result = resolver.resolve(basicAxe, new AttributeEngine());

		assertEquals(10f, result.getValue(DefaultAttributes.ATTACK_DAMAGE));
		assertEquals(1.2f, result.getValue(DefaultAttributes.ATTACK_SPEED));
	}

	/**
	 * Test case 2: Structured Component with Simple Attributes
	 * Attributes from different parts should be aggregated.
	 */
	@Test
	void testStructuredEquipmentWithOnlySimpleAttributes() {
		StaticComponent head = part(PICKAXE_HEAD_ID, PICKAXE_HEAD_TAG, List.of(new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f)));
		StaticComponent handle = part(HANDLE_ID, HANDLE_TAG, List.of(new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f)));

		Map<String, List<?>> baseProperties = new HashMap<>();
		baseProperties.put(Attribute.KEY.key(), List.of(new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 3f)));

		StructuredEquipment pickaxe = new StructuredEquipment(
				PICKAXE_ID,
				Set.of(idFactory.of("pickaxe")),
				baseProperties,
				new ComponentStructure(slotsMap(
						slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head),
						slot(HANDLE_SLOT_ID, HANDLE_TAG, handle)
				))
		);

		AttributeQueryResult result = resolver.resolve(pickaxe, new AttributeEngine());
		assertEquals(10f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), "Total attack damage should be base + head + handle (3+5+2=10)");
	}

	/**
	 * Test case 3: Multiple Attribute Types
	 * Different attribute types should be aggregated independently.
	 */
	@Test
	void testMixedAttributeTypes() {
		SimpleAttribute baseMiningSpeed = new SimpleAttribute(DefaultAttributes.MINING_SPEED, 2.0f);
		SimpleAttribute headMiningSpeed = new SimpleAttribute(DefaultAttributes.MINING_SPEED, 3.0f);
		SimpleAttribute headDamage = new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5.0f);

		StaticComponent head = part(PICKAXE_HEAD_ID, PICKAXE_HEAD_TAG, List.of(headMiningSpeed, headDamage));
		StaticComponent handle = part(HANDLE_ID, HANDLE_TAG, List.of());

		Map<String, List<?>> baseProperties = new HashMap<>();
		baseProperties.put(Attribute.KEY.key(), List.of(baseMiningSpeed));

		StructuredEquipment tool = new StructuredEquipment(
				PICKAXE_ID,
				Set.of(idFactory.of("tool")),
				baseProperties,
				new ComponentStructure(slotsMap(
						slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head),
						slot(HANDLE_SLOT_ID, HANDLE_TAG, handle)
				))
		);

		AttributeQueryResult result = resolver.resolve(tool, new AttributeEngine());

		assertEquals(5.0f, result.getValue(DefaultAttributes.MINING_SPEED), "Mining Speed should be base + head (2.0 + 3.0 = 5.0)");
		assertEquals(5.0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), "Attack Damage should be from head only (5.0)");
	}
}
