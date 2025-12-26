package com.sigmundgranaas.forgero.core.component.mutation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
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
		mutater = mutater();

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
				.withUpgradeSlot(upgradeSlot(BINDING_SLOT_ID, BINDING_SLOT_TYPE, c -> c.getTags().contains(BINDING_SLOT_TYPE), originalBinding))
				.withUpgradeSlot(upgradeSlot(GEM_SLOT_ID, GEM_TAG, c -> c.getTags().contains(GEM_TAG)))
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
		// This test verifies that the factory/builder properly validates duplicate slot IDs
		// Since we're testing through the API, we create the component and expect the validation
		// to occur during construction
		assertThrows(IllegalArgumentException.class, () -> {
			tool(PICKAXE_ID)
					.withPart(originalHead, HEAD_SLOT_ID.toString(), PICKAXE_HEAD_TAG)
					.withUpgradeSlot(upgradeSlot(HEAD_SLOT_ID, BINDING_SLOT_TYPE))
					.build();
		}, "Should throw when a slot ID is duplicated between structure and upgrades.");
	}

	@Test
	void testComponentStructureConstructorThrowsOnIdMismatch() {
		// This test verifies internal validation logic - the API should prevent this scenario
		// by constructing slots with matching IDs. We test that invalid construction fails.
		var invalidSlot = structureSlot(HEAD_SLOT_ID.toString(), PICKAXE_HEAD_TAG, originalHead);
		var wrongIdMap = java.util.Map.of(id("wrong_id"), invalidSlot);

		assertThrows(IllegalArgumentException.class, () ->
						new com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure(wrongIdMap),
				"ComponentStructure constructor should throw if map key and slot's internal ID do not match.");
	}
}
