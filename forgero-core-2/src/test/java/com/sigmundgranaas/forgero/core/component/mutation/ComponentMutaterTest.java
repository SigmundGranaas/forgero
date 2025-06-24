package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.variant.StaticPart;
import com.sigmundgranaas.forgero.core.component.variant.StructuredExtensibleEquipment;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ComponentMutaterTest extends ForgeroTest {
	private ComponentMutater mutater;

	// Component parts
	private StaticPart originalHead;
	private StaticPart originalHandle;
	private StaticPart originalBinding;
	private StaticPart diamondGem;
	private StaticPart newHandle;
	private StaticPart invalidHandle;

	// Full component
	private StructuredExtensibleEquipment pickaxe;

	// Slot identifiers
	private static final OpenIdentifier HEAD_SLOT_ID = idFactory.of("pickaxe-head_slot");
	private static final OpenIdentifier HANDLE_SLOT_ID = idFactory.of("pickaxe-handle_slot");
	private static final OpenIdentifier BINDING_SLOT_ID = idFactory.of("pickaxe-binding_slot");
	private static final OpenIdentifier GEM_SLOT_ID = idFactory.of("pickaxe-gem_slot");

	@BeforeEach
	void setUp() {
		mutater = new ComponentMutaterImpl();

		// Setup parts with correct tags for slot type matching
		originalHead = part(PICKAXE_HEAD_ID, Set.of(PICKAXE_HEAD_TAG));
		originalHandle = part(HANDLE_ID, Set.of(HANDLE_TAG));
		originalBinding = part(idFactory.of("iron_binding"), Set.of(BINDING_TAG));
		diamondGem = part(DIAMOND_ID, Set.of(GEM_TAG));
		newHandle = part(idFactory.of("new_oak_handle"), Set.of(HANDLE_TAG));
		invalidHandle = part(idFactory.of("invalid_handle"), Set.of(WOOD_TAG)); // Does not have HANDLE_TAG

		// Setup Structure with unique slot IDs
		ComponentStructure structure = new ComponentStructure(List.of(
				new StructureSlot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, "Head slot", originalHead),
				new StructureSlot(HANDLE_SLOT_ID, HANDLE_TAG, "Handle slot", originalHandle)
		));

		// Setup Upgrades with unique slot IDs
		ComponentUpgrades upgrades = new ComponentUpgrades(List.of(
				new UpgradeSlot(BINDING_SLOT_ID, BINDING_TAG, "Binding slot", c -> c.getTags().contains(BINDING_TAG), Optional.of(originalBinding)),
				new UpgradeSlot(GEM_SLOT_ID, GEM_TAG, "Gem slot", c -> c.getTags().contains(GEM_TAG), Optional.empty())
		));

		pickaxe = new StructuredExtensibleEquipment(PICKAXE_ID, Set.of(idFactory.of("pickaxe")), Collections.emptyList(), structure, upgrades);
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
		StaticPart invalidGem = part(idFactory.of("not_a_gem"), WOOD_TAG);
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
		ComponentStructure structure = new ComponentStructure(List.of(
				new StructureSlot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, "Head slot", originalHead)
		));

		ComponentUpgrades upgradesWithDuplicate = new ComponentUpgrades(List.of(
				new UpgradeSlot(HEAD_SLOT_ID, BINDING_TAG, "Binding slot", c -> true, Optional.empty())
		));

		assertThrows(IllegalArgumentException.class, () -> new StructuredExtensibleEquipment(
						PICKAXE_ID, Set.of(), Collections.emptyList(), structure, upgradesWithDuplicate),
				"Should throw when a slot ID is duplicated between structure and upgrades."
		);
	}
}
