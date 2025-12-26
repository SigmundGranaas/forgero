package com.sigmundgranaas.forgero.core.component;

import static com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes.ATTACK_DAMAGE;
import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class ComponentConstructionTest extends ForgeroTest {

	@Test
	void testStaticPart() {
		var part = part(IRON_ID)
				.withTag(METAL_TAG)
				.withAttribute(ATTACK_DAMAGE, 10)
				.build();

		assertEquals(IRON_ID, part.id());
		assertTrue(part.getTags().contains(METAL_TAG));

		List<?> attributesFromMap = part.properties(Attribute.KEY);
		assertInstanceOf(List.class, attributesFromMap, "Properties map should contain a list of attributes under the ATTRIBUTE_KEY");
		Optional<SimpleAttribute> simpleAttribute = attributesFromMap.stream()
				.filter(SimpleAttribute.class::isInstance)
				.map(SimpleAttribute.class::cast)
				.findFirst();
		assertTrue(simpleAttribute.isPresent(), "Should find a SimpleAttribute in the properties list.");
		assertEquals(10, simpleAttribute.get().value());
	}

	@Test
	void testExtensiblePart() {
		var part = part(IRON_ID)
				.withTag(METAL_TAG)
				.withUpgradeSlot(upgradeSlot(GEM_SLOT_ID.toString(), GEM_SLOT_TYPE_TAG))
				.build();

		assertEquals(1, part.getUpgradeSlots().size());
		assertInstanceOf(CustomizableComponent.class, part);
	}

	@Test
	void testStructuredPart() {
		var material = material(IRON_ID, METAL_TAG);
		var schematic = schematic(PICKAXE_HEAD_ID);

		var part = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, material))
				.withStructureSlot(structureSlot("schematic_slot", PICKAXE_HEAD_SHAPE_ID, schematic))
				.build();

		assertEquals(2, part.getChildren().size());
		assertInstanceOf(StructuredComponent.class, part);
	}

	@Test
	void testStructuredExtensiblePart() {
		var material = material(IRON_ID, METAL_TAG);

		var part = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, material))
				.withUpgradeSlot(upgradeSlot(GEM_SLOT_ID.toString(), GEM_SLOT_TYPE_TAG))
				.build();

		assertEquals(1, part.structure().slots().size());
		assertEquals(1, part.getUpgradeSlots().size());
		assertInstanceOf(StructuredComponent.class, part);
		assertInstanceOf(CustomizableComponent.class, part);
	}

	@Test
	void testStaticEquipment() {
		var equipment = tool(PICKAXE_ID)
				.withAttribute(ATTACK_DAMAGE, 5)
				.build();

		List<Attribute> attributesFromMap = equipment.properties(Attribute.KEY);
		assertInstanceOf(List.class, attributesFromMap, "Properties map should contain a list of attributes under the ATTRIBUTE_KEY");
		Optional<SimpleAttribute> simpleAttribute = attributesFromMap.stream()
				.filter(SimpleAttribute.class::isInstance)
				.map(SimpleAttribute.class::cast)
				.findFirst();
		assertTrue(simpleAttribute.isPresent(), "Should find a SimpleAttribute in the properties list.");
		assertEquals(5, simpleAttribute.get().value());
	}

	@Test
	void testExtensibleEquipment() {
		var equipment = tool(PICKAXE_ID)
				.withUpgradeSlot(upgradeSlot(GEM_SLOT_ID.toString(), GEM_SLOT_TYPE_TAG))
				.build();

		assertEquals(1, equipment.getUpgradeSlots().size());
	}

	@Test
	void testStructuredEquipment() {
		var head = part(PICKAXE_HEAD_ID).withTag(PICKAXE_HEAD_TAG).build();
		var handle = part(HANDLE_ID).withTag(HANDLE_TAG).build();

		var equipment = tool(PICKAXE_ID)
				.withPart(head, HEAD_SLOT_ID.toString(), PICKAXE_HEAD_TAG)
				.withPart(handle, HANDLE_SLOT_ID.toString(), HANDLE_TAG)
				.build();

		assertEquals(2, equipment.getChildren().size());
	}

	@Test
	void testStructuredExtensibleEquipment() {
		var head = part(PICKAXE_HEAD_ID).withTag(PICKAXE_HEAD_TAG).build();
		var handle = part(HANDLE_ID).withTag(HANDLE_TAG).build();

		var equipment = tool(PICKAXE_ID)
				.withPart(head, HEAD_SLOT_ID.toString(), PICKAXE_HEAD_TAG)
				.withPart(handle, HANDLE_SLOT_ID.toString(), HANDLE_TAG)
				.withUpgradeSlot(upgradeSlot(BINDING_SLOT_ID.toString(), BINDING_TAG))
				.build();

		assertEquals(2, equipment.structure().slots().size());
		assertEquals(1, equipment.upgrades().slots().size());
	}
}
