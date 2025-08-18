package com.sigmundgranaas.forgero.core.component.mutation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.impl.StructuredExtensibleEquipment;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.testutils.TestIdentifiers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.part;
import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.tool;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class ComponentMutaterImplTest {
	private ComponentMutater mutater;

	// Component parts
	private Component originalHead;
	private Component originalHandle;
	private Component originalBinding;
	private Component diamondGem;
	private Component newHandle;
	private Component invalidHandle;

	// Full component
	private Component pickaxe;

	// Slot identifiers
	private static final OpenIdentifier HEAD_SLOT_ID = id("pickaxe-head_slot");
	private static final OpenIdentifier HANDLE_SLOT_ID = id("pickaxe-handle_slot");
	private static final OpenIdentifier BINDING_SLOT_ID = id("pickaxe-binding_slot");
	private static final OpenIdentifier GEM_SLOT_ID = id("pickaxe-gem_slot");

	@BeforeEach
	void setUp() {
		mutater = new ComponentMutaterImpl();

		// Setup parts with correct tags for slot type matching
		originalHead = part(PICKAXE_HEAD_ID).withTag(PICKAXE_HEAD_TAG).build();
		originalHandle = part(HANDLE_ID).withTag(HANDLE_TAG).build();
		originalBinding = part("iron_binding").withTag(BINDING_SLOT_TYPE).build();
		diamondGem = part(DIAMOND_ID).withTag(GEM_TAG).build();
		newHandle = part("new_oak_handle").withTag(HANDLE_TAG).build();
		invalidHandle = part("invalid_handle").withTag(WOOD_TAG).build(); // Does not have HANDLE_TAG

		pickaxe = tool(PICKAXE_ID).withTag("pickaxe")
				.withPart(originalHead, "pickaxe-head_slot", PICKAXE_HEAD_TAG)
				.withPart(originalHandle, "pickaxe-handle_slot", HANDLE_TAG)
				.withUpgradeSlot(new UpgradeSlot(BINDING_SLOT_ID, BINDING_SLOT_TYPE, "Binding slot", c -> c.getTags().contains(BINDING_SLOT_TYPE), Optional.of(originalBinding)))
				.withUpgradeSlot(new UpgradeSlot(GEM_SLOT_ID, GEM_TAG, "Gem slot", c -> c.getTags().contains(GEM_TAG), Optional.empty()))
				.build();
	}

	@Test
	void testReplaceHandleInStructureSlot() {
		Component newPickaxe = mutater.setSlot(pickaxe, HANDLE_SLOT_ID, newHandle);

		assertNotEquals(pickaxe, newPickaxe, "A new component instance should be returned.");

		Optional<Component> handleInNew = mutater.findSlot(newPickaxe, HANDLE_SLOT_ID).flatMap(Slot::get);
		assertTrue(handleInNew.isPresent());
		assertEquals(newHandle, handleInNew.get());

		Optional<Component> headInNew = mutater.findSlot(newPickaxe, HEAD_SLOT_ID).flatMap(Slot::get);
		assertTrue(headInNew.isPresent());
		assertSame(originalHead, headInNew.get(), "Unchanged parts should be the same instance.");
	}

	@Test
	void testSettingIncompatiblePartInStructureSlotThrows() {
		assertThrows(IllegalArgumentException.class, () -> mutater.setSlot(pickaxe, HANDLE_SLOT_ID, invalidHandle),
				"Should not be able to insert a part that does not match the slot's required type tag.");
	}

	@Test
	void testAddGemToEmptyUpgradeSlot() {
		Component newPickaxe = mutater.setSlot(pickaxe, GEM_SLOT_ID, diamondGem);

		assertNotEquals(pickaxe, newPickaxe);

		Optional<Component> gemInNew = mutater.findSlot(newPickaxe, GEM_SLOT_ID).flatMap(Slot::get);
		assertTrue(gemInNew.isPresent());
		assertEquals(diamondGem, gemInNew.get());
	}

	@Test
	void testRemoveBindingFromFilledUpgradeSlot() {
		Component newPickaxe = mutater.removeSlot(pickaxe, BINDING_SLOT_ID);

		assertNotEquals(pickaxe, newPickaxe);

		Optional<Component> bindingInNew = mutater.findSlot(newPickaxe, BINDING_SLOT_ID).flatMap(Slot::get);
		assertTrue(bindingInNew.isEmpty(), "The binding slot should now be empty.");
	}

	@Test
	void testRemovingFromStructureSlotThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> mutater.removeSlot(pickaxe, HEAD_SLOT_ID),
				"Should not be able to remove a required structural part.");
	}

	@Test
	void testSettingInvalidComponentInUpgradeSlotThrowsException() {
		Component invalidGem = part("not_a_gem").withTag(WOOD_TAG).build();
		assertThrows(IllegalArgumentException.class, () -> mutater.setSlot(pickaxe, GEM_SLOT_ID, invalidGem),
				"Should fail predicate validation for gem slot.");
	}

	@Test
	void testIdenticalChangesProduceEqualObjectsForCaching() {
		Component newPickaxe1 = mutater.setSlot(pickaxe, HANDLE_SLOT_ID, newHandle);
		Component newPickaxe2 = mutater.setSlot(pickaxe, HANDLE_SLOT_ID, newHandle);

		assertEquals(newPickaxe1, newPickaxe2, "Two identical modifications should result in equal objects.");
		assertEquals(newPickaxe1.hashCode(), newPickaxe2.hashCode(), "Hash codes should also be equal for cacheability.");
	}

	@Test
	void testCreatingComponentWithDuplicateSlotIdsThrows() {
		ComponentStructure structure = new ComponentStructure(Map.of(
				HEAD_SLOT_ID, new StructureSlot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, "Head slot", originalHead)
		));

		ComponentUpgrades upgradesWithDuplicate = new ComponentUpgrades(List.of(
				new UpgradeSlot(HEAD_SLOT_ID, BINDING_SLOT_TYPE, "Binding slot", c -> true, Optional.empty())
		));

		assertThrows(IllegalArgumentException.class, () -> new StructuredExtensibleEquipment(
						PICKAXE_ID, Set.of(), new HashMap<>(), structure, upgradesWithDuplicate),
				"Should throw when a slot ID is duplicated between structure and upgrades."
		);
	}

	@Test
	void testComponentStructureConstructorThrowsOnIdMismatch() {
		Map<OpenIdentifier, StructureSlot> invalidStructureSlots = Map.of(
				TestIdentifiers.id("wrong_id"), new StructureSlot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, "Head slot", originalHead)
		);
		assertThrows(IllegalArgumentException.class, () -> new ComponentStructure(invalidStructureSlots),
				"ComponentStructure constructor should throw if map key and slot's internal ID do not match.");
	}
}
