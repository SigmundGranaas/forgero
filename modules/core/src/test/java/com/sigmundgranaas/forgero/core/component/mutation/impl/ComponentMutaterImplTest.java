package com.sigmundgranaas.forgero.core.component.mutation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.ForgeroTest;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class ComponentMutaterImplTest extends ForgeroTest {
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
				.withUpgradeSlot(upgradeSlot(BINDING_SLOT_ID.toString(), BINDING_SLOT_TYPE, c -> c.getTags().contains(BINDING_SLOT_TYPE), originalBinding))
				.withUpgradeSlot(upgradeSlot(GEM_SLOT_ID.toString(), GEM_TAG, c -> c.getTags().contains(GEM_TAG)))
				.build();
	}

	/**
	 * Helper to find and extract Component content from either a structure part or upgrade slot.
	 * Searches both structure parts (ComponentPart) and mutable slots (ComponentUpgradeSlot).
	 */
	private Optional<Component> findContentById(Component component, OpenIdentifier id) {
		// Check structure parts first
		if (component instanceof com.sigmundgranaas.forgero.core.component.api.StructuredComponent structured) {
			var part = structured.structure().getPart(id);
			if (part.isPresent()) {
				return Optional.of(part.get().getContent());
			}
		}

		// Check mutable slots
		var slot = mutater.findSlot(component, id);
		if (slot.isPresent() && slot.get() instanceof ComponentUpgradeSlot upgradeSlot) {
			return upgradeSlot.getContent();
		}

		return Optional.empty();
	}

	@Test
	void testReplaceHandleInStructureSlot() {
		Component newPickaxe = mutater.setSlot(pickaxe, HANDLE_SLOT_ID, newHandle);

		assertNotEquals(pickaxe, newPickaxe, "A new component instance should be returned.");

		Optional<Component> handleInNew = findContentById(newPickaxe, HANDLE_SLOT_ID);
		assertTrue(handleInNew.isPresent());
		assertEquals(newHandle, handleInNew.get());

		Optional<Component> headInNew = findContentById(newPickaxe, HEAD_SLOT_ID);
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

		Optional<Component> gemInNew = findContentById(newPickaxe, GEM_SLOT_ID);
		assertTrue(gemInNew.isPresent());
		assertEquals(diamondGem, gemInNew.get());
	}

	@Test
	void testRemoveBindingFromFilledUpgradeSlot() {
		Component newPickaxe = mutater.removeSlot(pickaxe, BINDING_SLOT_ID);

		assertNotEquals(pickaxe, newPickaxe);

		Optional<Component> bindingInNew = findContentById(newPickaxe, BINDING_SLOT_ID);
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
					.withUpgradeSlot(upgradeSlot(HEAD_SLOT_ID.toString(), BINDING_SLOT_TYPE))
					.build();
		}, "Should throw when a slot ID is duplicated between structure and upgrades.");
	}

	@Test
	void testComponentStructureConstructorThrowsOnIdMismatch() {
		// This test verifies that SlotContainer validates uniqueness of slot IDs.
		// Duplicate slot IDs should result in an exception to ensure data integrity.
		var slot1 = structureSlot(HEAD_SLOT_ID.toString(), PICKAXE_HEAD_TAG, originalHead);
		var slot2 = structureSlot(HEAD_SLOT_ID.toString(), HANDLE_TAG, originalHandle);

		// Creating a structure with both slots having the same ID should throw
		assertThrows(IllegalArgumentException.class,
				() -> com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure.of(slot1, slot2),
				"Should throw when duplicate slot IDs are provided.");
	}

	@Nested
	@DisplayName("Slot Searching Tests")
	class SlotSearchingTests {

		@Test
		void findsSlotInDirectChildren() {
			// findSlot() only finds mutable upgrade slots, not structure slots
			// Test finding upgrade slot in direct children
			Optional<Slot> gemSlot = mutater.findSlot(pickaxe, GEM_SLOT_ID);
			assertTrue(gemSlot.isPresent(), "Should find gem upgrade slot in direct children");
			assertEquals(GEM_SLOT_ID, gemSlot.get().id());

			// Test finding filled upgrade slot
			Optional<Slot> bindingSlot = mutater.findSlot(pickaxe, BINDING_SLOT_ID);
			assertTrue(bindingSlot.isPresent(), "Should find binding upgrade slot in direct children");
			assertEquals(BINDING_SLOT_ID, bindingSlot.get().id());
		}

		@Test
		void searchesOnlyDirectChildrenNotNestedSlots() {
			// Create a nested structure: sword -> blade (with upgrade slot for gems)
			OpenIdentifier bladeGemSlotId = id("blade-gem_slot");
			Component blade = part(id("sword_blade"))
					.withTag(SWORD_BLADE_TAG)
					.withUpgradeSlot(upgradeSlot(bladeGemSlotId.toString(), GEM_TAG, c -> c.getTags().contains(GEM_TAG)))
					.build();
			Component sword = tool(SWORD_ID)
					.withPart(blade, "sword-blade_slot", SWORD_BLADE_TAG)
					.build();

			// findSlot() only searches direct children, not nested slots
			Optional<Slot> nestedSlot = mutater.findSlot(sword, bladeGemSlotId);
			assertFalse(nestedSlot.isPresent(),
					"findSlot() should NOT find slots nested in child components (only direct children)");

			// But we can find the slot if we search on the blade directly
			Optional<Slot> directSlot = mutater.findSlot(blade, bladeGemSlotId);
			assertTrue(directSlot.isPresent(),
					"findSlot() should find slot when searching directly on the component that contains it");
		}

		@Test
		void distinguishesStructureVsUpgradeSlots() {
			// Structure slots (ComponentPart) are not returned by findSlot()
			// Only mutable upgrade slots are returned
			Optional<Slot> structureSlot = mutater.findSlot(pickaxe, HEAD_SLOT_ID);
			assertFalse(structureSlot.isPresent(),
					"findSlot() should NOT find structure slots (ComponentPart), only mutable upgrade slots");

			// Find upgrade slot
			Optional<Slot> upgradeSlot = mutater.findSlot(pickaxe, GEM_SLOT_ID);
			assertTrue(upgradeSlot.isPresent(),
					"findSlot() should find upgrade slots");
			assertTrue(upgradeSlot.get() instanceof ComponentUpgradeSlot,
					"GEM_SLOT should be a ComponentUpgradeSlot");
		}

		@Test
		void returnsEmptyForNonexistentSlot() {
			OpenIdentifier nonexistentId = id("nonexistent_slot");
			Optional<Slot> result = mutater.findSlot(pickaxe, nonexistentId);
			assertFalse(result.isPresent(), "Should return empty Optional for nonexistent slot");
		}
	}

	@Nested
	@DisplayName("Immutability Tests")
	class ImmutabilityTests {

		@Test
		void originalComponentUnchangedAfterMutation() {
			// Capture original state
			Optional<Component> originalHandleInPickaxe = findContentById(pickaxe, HANDLE_SLOT_ID);
			assertTrue(originalHandleInPickaxe.isPresent());
			Component originalHandleComponent = originalHandleInPickaxe.get();

			// Mutate
			Component newPickaxe = mutater.setSlot(pickaxe, HANDLE_SLOT_ID, newHandle);

			// Original should be unchanged
			Optional<Component> handleAfterMutation = findContentById(pickaxe, HANDLE_SLOT_ID);
			assertTrue(handleAfterMutation.isPresent());
			assertSame(originalHandleComponent, handleAfterMutation.get(),
					"Original component should not be modified");
			assertNotEquals(pickaxe, newPickaxe,
					"Mutation should return a different instance");
		}

		@Test
		void mutationReturnsNewInstance() {
			Component newPickaxe = mutater.setSlot(pickaxe, GEM_SLOT_ID, diamondGem);

			assertNotSame(pickaxe, newPickaxe,
					"setSlot should return a new instance, not modify the original");
			assertNotEquals(pickaxe, newPickaxe,
					"New instance should not be equal to original when content differs");
		}

		@Test
		void deepCopyPreservesStructure() {
			// Use the existing pickaxe from setUp which has head + handle structure
			// Mutate the handle
			Component newPickaxe = mutater.setSlot(pickaxe, HANDLE_SLOT_ID, newHandle);

			// The head should be preserved (same instance) since we only changed the handle
			Optional<Component> originalHeadOpt = findContentById(pickaxe, HEAD_SLOT_ID);
			Optional<Component> newHeadOpt = findContentById(newPickaxe, HEAD_SLOT_ID);

			assertTrue(originalHeadOpt.isPresent(), "Original pickaxe should have head");
			assertTrue(newHeadOpt.isPresent(), "New pickaxe should have head");
			assertSame(originalHeadOpt.get(), newHeadOpt.get(),
					"Unchanged parts should be preserved as same instance for efficiency");
		}
	}

	@Nested
	@DisplayName("Validation Tests")
	class ValidationTests {

		@Test
		void rejectsInvalidSlotContent() {
			Component invalidComponent = part("invalid_part").withTag(WOOD_TAG).build();

			// Try to set gem slot with non-gem component
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> mutater.setSlot(pickaxe, GEM_SLOT_ID, invalidComponent),
					"Should reject component that doesn't match slot predicate");

			assertNotNull(exception.getMessage(),
					"Exception should have a message");
		}

		@Test
		void validatesSlotTypeCompatibility() {
			// Try to set handle slot with a component that doesn't have HANDLE_TAG
			Component incompatibleComponent = part("incompatible")
					.withTag(WOOD_TAG)
					.build();

			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> mutater.setSlot(pickaxe, HANDLE_SLOT_ID, incompatibleComponent),
					"Should reject component without required tag for structure slot");

			assertNotNull(exception.getMessage());
		}

		@Test
		void producesUsefulErrorMessages() {
			Component invalidGem = part("fake_gem").withTag(WOOD_TAG).build();

			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> mutater.setSlot(pickaxe, GEM_SLOT_ID, invalidGem));

			String message = exception.getMessage();
			assertNotNull(message, "Exception should have a message");
			assertFalse(message.isEmpty(), "Error message should not be empty");
			// Could also check that message contains relevant information like slot ID or component ID
		}
	}
}
