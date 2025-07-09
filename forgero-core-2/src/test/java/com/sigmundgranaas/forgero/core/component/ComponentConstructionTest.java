package com.sigmundgranaas.forgero.core.component;

import static com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes.ATTACK_DAMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sigmundgranaas.forgero.core.ForgeroTest;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

class ComponentConstructionTest extends ForgeroTest {

	@Test
	void testStaticPart() {
		var part = new StaticComponent(IRON_ID, Set.of(METAL_TAG), List.of(attribute(ATTACK_DAMAGE, 10)));
		assertEquals(IRON_ID, part.id());
		assertTrue(part.getTags().contains(METAL_TAG));
		assertEquals(10, part.getProperties().stream().filter(SimpleAttribute.class::isInstance).map(SimpleAttribute.class::cast).findFirst().get().value());
	}

	@Test
	void testExtensiblePart() {
		var slot = new UpgradeSlot(GEM_SLOT_ID, GEM_SLOT_TYPE_TAG, "Gem slot", (comp) -> true, Optional.empty());
		var upgrades = new ComponentUpgrades(List.of(slot));
		var part = new ExtensiblePart(IRON_ID, Set.of(METAL_TAG), List.of(), upgrades);
		assertEquals(1, part.getUpgradeSlots().size());
		assertInstanceOf(CustomizableComponent.class, part);
	}

	@Test
	void testStructuredPart() {
		var material = material(IRON_ID, METAL_TAG);
		var schematic = schematic(PICKAXE_HEAD_ID);
		var structure = new ComponentStructure(List.of(slot(idFactory.of("material_slot"), MATERIAL_ID, material), slot(idFactory.of("schematic_slot"), SCHEMATIC_ID, schematic)));
		var part = new StructuredPart(PICKAXE_HEAD_ID, Set.of(), List.of(), structure);
		assertEquals(2, part.getChildren().size());
		assertInstanceOf(StructuredComponent.class, part);
	}

	@Test
	void testStructuredExtensiblePart() {
		var material = material(IRON_ID, METAL_TAG);
		var structure = new ComponentStructure(List.of(slot(idFactory.of("material_slot"), MATERIAL_ID, material)));
		var slot = new UpgradeSlot(GEM_SLOT_ID, GEM_SLOT_TYPE_TAG, "Gem slot", (comp) -> true, Optional.empty());
		var upgrades = new ComponentUpgrades(List.of(slot));
		var part = new StructuredExtensiblePart(PICKAXE_HEAD_ID, Set.of(), List.of(), structure, upgrades);
		assertEquals(1, part.structure().slots().size());
		assertEquals(1, part.getUpgradeSlots().size());
		assertInstanceOf(StructuredComponent.class, part);
		assertInstanceOf(CustomizableComponent.class, part);
	}

	@Test
	void testStaticEquipment() {
		var equipment = new StaticEquipment(PICKAXE_ID, Set.of(), List.of(attribute(ATTACK_DAMAGE, 5)));
		assertEquals(5, equipment.getProperties().stream().filter(SimpleAttribute.class::isInstance).map(SimpleAttribute.class::cast).findFirst().get().value());
	}

	@Test
	void testExtensibleEquipment() {
		var slot = new UpgradeSlot(GEM_SLOT_ID, GEM_SLOT_TYPE_TAG, "Gem slot", (comp) -> true, Optional.empty());
		var upgrades = new ComponentUpgrades(List.of(slot));
		var equipment = new ExtensibleEquipment(PICKAXE_ID, Set.of(), List.of(), upgrades);
		assertEquals(1, equipment.getUpgradeSlots().size());
	}

	@Test
	void testStructuredEquipment() {
		var head = new StaticComponent(PICKAXE_HEAD_ID, Set.of(PICKAXE_HEAD_TAG), List.of());
		var handle = new StaticComponent(HANDLE_ID, Set.of(HANDLE_TAG), List.of());
		var structure = new ComponentStructure(List.of(slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head), slot(HANDLE_SLOT_ID, HANDLE_TAG, handle)));
		var equipment = new StructuredEquipment(PICKAXE_ID, Set.of(), List.of(), structure);
		assertEquals(2, equipment.getChildren().size());
	}

	@Test
	void testStructuredExtensibleEquipment() {
		var head = new StaticComponent(PICKAXE_HEAD_ID, Set.of(PICKAXE_HEAD_TAG), List.of());
		var handle = new StaticComponent(HANDLE_ID, Set.of(HANDLE_TAG), List.of());
		var structure = new ComponentStructure(List.of(slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head), slot(HANDLE_SLOT_ID, HANDLE_TAG, handle)));
		var upgradeSlot = new UpgradeSlot(BINDING_SLOT_ID, BINDING_TAG, "Binding slot", (comp) -> true, Optional.empty());
		var upgrades = new ComponentUpgrades(List.of(upgradeSlot));
		var equipment = new StructuredExtensibleEquipment(PICKAXE_ID, Set.of(), List.of(), structure, upgrades);
		assertEquals(2, equipment.structure().slots().size());
		assertEquals(1, equipment.upgrades().slots().size());
	}
}
