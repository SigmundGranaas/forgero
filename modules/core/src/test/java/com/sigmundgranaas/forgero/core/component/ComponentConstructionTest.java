package com.sigmundgranaas.forgero.core.component;

import static com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes.ATTACK_DAMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute; // Import the Attribute interface to access Attribute.KEY
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.ExtensibleEquipment;
import com.sigmundgranaas.forgero.core.component.impl.ExtensiblePart;
import com.sigmundgranaas.forgero.core.component.impl.StaticEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.component.impl.StructuredEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StructuredExtensibleEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StructuredExtensiblePart;
import com.sigmundgranaas.forgero.core.component.impl.StructuredPart;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class ComponentConstructionTest extends ForgeroTest {

	@Test
	void testStaticPart() {
		// New properties map for attributes
		Map<String, List<?>> properties = new HashMap<>();
		properties.put(Attribute.KEY.key(), List.of(attribute(ATTACK_DAMAGE, 10)));

		// Assuming StaticComponent constructor now takes Map<String, List<?>> for properties
		var part = new StaticComponent(IRON_ID, Set.of(METAL_TAG), properties);

		assertEquals(IRON_ID, part.id());
		assertTrue(part.getTags().contains(METAL_TAG));

		// Updated assertion: retrieve the list of attributes from the map using Attribute.KEY.key()
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
		var slot = new UpgradeSlot(GEM_SLOT_ID, GEM_SLOT_TYPE_TAG, "Gem slot", (comp) -> true, Optional.empty());
		var upgrades = new ComponentUpgrades(List.of(slot));

		// Pass an empty HashMap for properties as there are no attributes
		var part = new ExtensiblePart(IRON_ID, Set.of(METAL_TAG), new HashMap<>(), upgrades);
		assertEquals(1, part.getUpgradeSlots().size());
		assertInstanceOf(CustomizableComponent.class, part);
	}

	@Test
	void testStructuredPart() {
		var material = material(IRON_ID, METAL_TAG);
		var schematic = schematic(PICKAXE_HEAD_ID);
		// Update ComponentStructure to use Map.of
		var structure = new ComponentStructure(slotsMap(slot(idFactory.of("material_slot"), TOOL_MATERIAL_ID, material), slot(idFactory.of("schematic_slot"), PICKAXE_HEAD_SHAPE_ID, schematic)));

		// Pass an empty HashMap for properties as there are no attributes
		var part = new StructuredPart(PICKAXE_HEAD_ID, Set.of(), new HashMap<>(), structure);
		assertEquals(2, part.getChildren().size());
		assertInstanceOf(StructuredComponent.class, part);
	}

	@Test
	void testStructuredExtensiblePart() {
		var material = material(IRON_ID, METAL_TAG);
		// Update ComponentStructure to use Map.of
		var structure = new ComponentStructure(slotsMap(slot(idFactory.of("material_slot"), TOOL_MATERIAL_ID, material)));
		var slot = new UpgradeSlot(GEM_SLOT_ID, GEM_SLOT_TYPE_TAG, "Gem slot", (comp) -> true, Optional.empty());
		var upgrades = new ComponentUpgrades(List.of(slot));

		// Pass an empty HashMap for properties as there are no attributes
		var part = new StructuredExtensiblePart(PICKAXE_HEAD_ID, Set.of(), new HashMap<>(), structure, upgrades);
		assertEquals(1, part.structure().slots().size());
		assertEquals(1, part.getUpgradeSlots().size());
		assertInstanceOf(StructuredComponent.class, part);
		assertInstanceOf(CustomizableComponent.class, part);
	}

	@Test
	void testStaticEquipment() {
		// New properties map for attributes
		Map<String, List<?>> properties = new HashMap<>();
		properties.put(Attribute.KEY.key(), List.of(attribute(ATTACK_DAMAGE, 5)));

		// Assuming StaticEquipment constructor now takes Map<String, List<?>> for properties
		var equipment = new StaticEquipment(PICKAXE_ID, Set.of(), properties);

		// Updated assertion: retrieve the list of attributes from the map using Attribute.KEY.key()
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
		var slot = new UpgradeSlot(GEM_SLOT_ID, GEM_SLOT_TYPE_TAG, "Gem slot", (comp) -> true, Optional.empty());
		var upgrades = new ComponentUpgrades(List.of(slot));

		// Pass an empty HashMap for properties as there are no attributes
		var equipment = new ExtensibleEquipment(PICKAXE_ID, Set.of(), new HashMap<>(), upgrades);
		assertEquals(1, equipment.getUpgradeSlots().size());
	}

	@Test
	void testStructuredEquipment() {
		var head = new StaticComponent(PICKAXE_HEAD_ID, Set.of(PICKAXE_HEAD_TAG), new HashMap<>()); // Updated: pass empty map
		var handle = new StaticComponent(HANDLE_ID, Set.of(HANDLE_TAG), new HashMap<>()); // Updated: pass empty map
		// Update ComponentStructure to use Map.of
		var structure = new ComponentStructure(slotsMap(slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head), slot(HANDLE_SLOT_ID, HANDLE_TAG, handle)));

		// Pass an empty HashMap for properties as there are no attributes
		var equipment = new StructuredEquipment(PICKAXE_ID, Set.of(), new HashMap<>(), structure);
		assertEquals(2, equipment.getChildren().size());
	}

	@Test
	void testStructuredExtensibleEquipment() {
		var head = new StaticComponent(PICKAXE_HEAD_ID, Set.of(PICKAXE_HEAD_TAG), new HashMap<>()); // Updated: pass empty map
		var handle = new StaticComponent(HANDLE_ID, Set.of(HANDLE_TAG), new HashMap<>()); // Updated: pass empty map
		// Update ComponentStructure to use Map.of
		var structure = new ComponentStructure(slotsMap(slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head), slot(HANDLE_SLOT_ID, HANDLE_TAG, handle)));
		var upgradeSlot = new UpgradeSlot(BINDING_SLOT_ID, BINDING_TAG, "Binding slot", (comp) -> true, Optional.empty());
		var upgrades = new ComponentUpgrades(List.of(upgradeSlot));

		// Pass an empty HashMap for properties as there are no attributes
		var equipment = new StructuredExtensibleEquipment(PICKAXE_ID, Set.of(), new HashMap<>(), structure, upgrades);
		assertEquals(2, equipment.structure().slots().size());
		assertEquals(1, equipment.upgrades().slots().size());
	}
}
