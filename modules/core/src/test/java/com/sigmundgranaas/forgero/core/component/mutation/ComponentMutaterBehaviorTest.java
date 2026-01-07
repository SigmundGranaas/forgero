package com.sigmundgranaas.forgero.core.component.mutation;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Behavior tests for ComponentMutater API.
 * Tests immutable mutation operations on components.
 */
@DisplayName("ComponentMutater")
class ComponentMutaterBehaviorTest extends ForgeroTest {

	private ComponentMutater mutater;

	@BeforeEach
	void setUp() {
		mutater = mutater();
	}

	@Nested
	@DisplayName("Mutation Builder")
	class MutationBuilderTests {

		@Test
		@DisplayName("should create empty mutation with no changes")
		void emptyMutationHasNoChanges() {
			var mutation = new ComponentMutater.Mutation.Builder().build();

			assertTrue(mutation.structure().isEmpty(), "Structure map should be empty");
			assertTrue(mutation.upgrades().isEmpty(), "Upgrades map should be empty");
			assertTrue(mutation.properties().isEmpty(), "Properties map should be empty");
		}

		@Test
		@DisplayName("should accumulate structure changes")
		void builderAccumulatesStructureChanges() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component oak = material(OAK_ID, WOOD_TAG);

			var mutation = new ComponentMutater.Mutation.Builder()
					.withStructure(id("slot1"), iron)
					.withStructure(id("slot2"), oak)
					.build();

			assertEquals(2, mutation.structure().size());
			assertEquals(iron, mutation.structure().get(id("slot1")));
			assertEquals(oak, mutation.structure().get(id("slot2")));
		}

		@Test
		@DisplayName("should accumulate upgrade changes")
		void builderAccumulatesUpgradeChanges() {
			Component gem1 = part(GEM_ID).withTag(GEM_TAG).build();
			Component gem2 = part(id("diamond_gem")).withTag(GEM_TAG).build();

			var mutation = new ComponentMutater.Mutation.Builder()
					.withUpgrade(id("gem_slot_1"), gem1)
					.withUpgrade(id("gem_slot_2"), gem2)
					.build();

			assertEquals(2, mutation.upgrades().size());
			assertEquals(gem1, mutation.upgrades().get(id("gem_slot_1")));
			assertEquals(gem2, mutation.upgrades().get(id("gem_slot_2")));
		}

		@Test
		@DisplayName("built mutation should be immutable")
		void builtMutationIsImmutable() {
			var mutation = new ComponentMutater.Mutation.Builder()
					.withStructure(id("slot1"), material(IRON_ID, METAL_TAG))
					.build();

			assertThrows(UnsupportedOperationException.class, () -> {
				mutation.structure().put(id("slot2"), material(OAK_ID, WOOD_TAG));
			});
		}
	}

	@Nested
	@DisplayName("setSlot - Structure Parts")
	class SetSlotStructureTests {

		@Test
		@DisplayName("should replace content in structure part")
		void replacesStructurePartContent() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component diamond = material(DIAMOND_ID, METAL_TAG);

			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withTag(PICKAXE_HEAD_TAG)
					.withStructureSlot(structureSlot("material", TOOL_MATERIAL_ID, iron))
					.build();

			Component result = mutater.setSlot(pickaxeHead, id("material"), diamond);

			assertNotSame(pickaxeHead, result, "Should return new component instance");
			assertTrue(result instanceof StructuredComponent);

			var structured = (StructuredComponent) result;
			var part = structured.structure().getPart(id("material"));
			assertTrue(part.isPresent());
			assertEquals(diamond, part.get().getContent());
		}

		@Test
		@DisplayName("should preserve other structure parts when replacing one")
		void preservesOtherStructureParts() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component oak = material(OAK_ID, WOOD_TAG);
			Component diamond = material(DIAMOND_ID, METAL_TAG);

			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withTag(PICKAXE_HEAD_TAG)
					.withStructureSlot(structureSlot("material", TOOL_MATERIAL_ID, iron))
					.withStructureSlot(structureSlot("binding", id("binding"), oak))
					.build();

			Component result = mutater.setSlot(pickaxeHead, id("material"), diamond);

			var structured = (StructuredComponent) result;
			var bindingPart = structured.structure().getPart(id("binding"));
			assertTrue(bindingPart.isPresent());
			assertEquals(oak, bindingPart.get().getContent());
		}

		@Test
		@DisplayName("should throw when structure part ID does not exist")
		void throwsWhenStructurePartNotFound() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withStructureSlot(structureSlot("material", TOOL_MATERIAL_ID, iron))
					.build();

			assertThrows(IllegalArgumentException.class, () -> {
				mutater.setSlot(pickaxeHead, id("nonexistent_slot"), iron);
			});
		}
	}

	@Nested
	@DisplayName("setSlot - Upgrade Slots")
	class SetSlotUpgradeTests {

		@Test
		@DisplayName("should fill empty upgrade slot")
		void fillsEmptyUpgradeSlot() {
			Component gem = part(GEM_ID).withTag(GEM_TAG).build();
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withTag(PICKAXE_HEAD_TAG)
					.withUpgradeSlot(upgradeSlot("gem_slot", GEM_SLOT_TYPE))
					.build();

			Component result = mutater.setSlot(pickaxeHead, id("gem_slot"), gem);

			assertNotSame(pickaxeHead, result);
			assertTrue(result instanceof CustomizableComponent);

			var customizable = (CustomizableComponent) result;
			var upgradeSlots = customizable.upgrades().allUpgradeSlots();
			assertEquals(1, upgradeSlots.size());

			var upgradeSlot = upgradeSlots.get(0);
			assertTrue(upgradeSlot.getContent().isPresent());
			assertEquals(gem, upgradeSlot.getContent().get());
		}

		@Test
		@DisplayName("should replace content in filled upgrade slot")
		void replacesFilledUpgradeSlotContent() {
			Component gem1 = part(GEM_ID).withTag(GEM_TAG).build();
			Component gem2 = part(id("diamond_gem")).withTag(GEM_TAG).build();

			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withTag(PICKAXE_HEAD_TAG)
					.withUpgradeSlot(upgradeSlot("gem_slot", GEM_SLOT_TYPE, c -> true, gem1))
					.build();

			Component result = mutater.setSlot(pickaxeHead, id("gem_slot"), gem2);

			var customizable = (CustomizableComponent) result;
			var upgradeSlot = customizable.upgrades().allUpgradeSlots().get(0);
			assertTrue(upgradeSlot.getContent().isPresent());
			assertEquals(gem2, upgradeSlot.getContent().get());
		}
	}

	@Nested
	@DisplayName("removeSlot")
	class RemoveSlotTests {

		@Test
		@DisplayName("should empty filled upgrade slot")
		void emptiesFilledUpgradeSlot() {
			Component gem = part(GEM_ID).withTag(GEM_TAG).build();
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withTag(PICKAXE_HEAD_TAG)
					.withUpgradeSlot(upgradeSlot("gem_slot", GEM_SLOT_TYPE, c -> true, gem))
					.build();

			Component result = mutater.removeSlot(pickaxeHead, id("gem_slot"));

			assertNotSame(pickaxeHead, result);
			var customizable = (CustomizableComponent) result;
			var upgradeSlot = customizable.upgrades().allUpgradeSlots().get(0);
			assertTrue(upgradeSlot.getContent().isEmpty(), "Slot content should be empty after removal");
		}

		@Test
		@DisplayName("should throw when trying to remove structure part")
		void throwsWhenRemovingStructurePart() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withStructureSlot(structureSlot("material", TOOL_MATERIAL_ID, iron))
					.build();

			assertThrows(IllegalArgumentException.class, () -> {
				mutater.removeSlot(pickaxeHead, id("material"));
			}, "Should not be able to remove structure parts");
		}

		@Test
		@DisplayName("should throw when slot ID does not exist")
		void throwsWhenSlotNotFound() {
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withUpgradeSlot(upgradeSlot("gem_slot", GEM_SLOT_TYPE))
					.build();

			assertThrows(IllegalArgumentException.class, () -> {
				mutater.removeSlot(pickaxeHead, id("nonexistent_slot"));
			});
		}

		@Test
		@DisplayName("should preserve other upgrade slots when removing one")
		void preservesOtherUpgradeSlots() {
			Component gem1 = part(GEM_ID).withTag(GEM_TAG).build();
			Component gem2 = part(id("diamond_gem")).withTag(GEM_TAG).build();

			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withTag(PICKAXE_HEAD_TAG)
					.withUpgradeSlot(upgradeSlot("gem_slot_1", GEM_SLOT_TYPE, c -> true, gem1))
					.withUpgradeSlot(upgradeSlot("gem_slot_2", GEM_SLOT_TYPE, c -> true, gem2))
					.build();

			Component result = mutater.removeSlot(pickaxeHead, id("gem_slot_1"));

			var customizable = (CustomizableComponent) result;
			var slots = customizable.upgrades().allUpgradeSlots();
			assertEquals(2, slots.size());

			// Find gem_slot_2 and verify it still has content
			var slot2 = slots.stream()
					.filter(s -> s.id().equals(id("gem_slot_2")))
					.findFirst()
					.orElseThrow();
			assertTrue(slot2.getContent().isPresent());
			assertEquals(gem2, slot2.getContent().get());
		}
	}

	@Nested
	@DisplayName("getAllSlots")
	class GetAllSlotsTests {

		@Test
		@DisplayName("should return empty list for component with no slots")
		void returnsEmptyListForStaticComponent() {
			Component iron = material(IRON_ID, METAL_TAG);

			List<Slot> slots = mutater.getAllSlots(iron);

			assertTrue(slots.isEmpty());
		}

		@Test
		@DisplayName("should return only upgrade slots, not structure parts")
		void returnsOnlyUpgradeSlots() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component gem = part(GEM_ID).withTag(GEM_TAG).build();

			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withTag(PICKAXE_HEAD_TAG)
					.withStructureSlot(structureSlot("material", TOOL_MATERIAL_ID, iron))
					.withUpgradeSlot(upgradeSlot("gem_slot", GEM_SLOT_TYPE, c -> true, gem))
					.build();

			List<Slot> slots = mutater.getAllSlots(pickaxeHead);

			assertEquals(1, slots.size(), "Should only return upgrade slots");
			assertEquals(id("gem_slot"), slots.get(0).id());
		}

		@Test
		@DisplayName("should return all upgrade slots from customizable component")
		void returnsAllUpgradeSlots() {
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withTag(PICKAXE_HEAD_TAG)
					.withUpgradeSlot(upgradeSlot("gem_slot_1", GEM_SLOT_TYPE))
					.withUpgradeSlot(upgradeSlot("gem_slot_2", GEM_SLOT_TYPE))
					.withUpgradeSlot(upgradeSlot("rune_slot", id("rune")))
					.build();

			List<Slot> slots = mutater.getAllSlots(pickaxeHead);

			assertEquals(3, slots.size());
		}

		@Test
		@DisplayName("returned list should be unmodifiable")
		void returnedListIsUnmodifiable() {
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withUpgradeSlot(upgradeSlot("gem_slot", GEM_SLOT_TYPE))
					.build();

			List<Slot> slots = mutater.getAllSlots(pickaxeHead);

			assertThrows(UnsupportedOperationException.class, () -> {
				slots.add(upgradeSlot("new_slot", GEM_SLOT_TYPE));
			});
		}
	}

	@Nested
	@DisplayName("findSlot")
	class FindSlotTests {

		@Test
		@DisplayName("should find upgrade slot by ID")
		void findsUpgradeSlotById() {
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withUpgradeSlot(upgradeSlot("gem_slot", GEM_SLOT_TYPE))
					.build();

			var slot = mutater.findSlot(pickaxeHead, id("gem_slot"));

			assertTrue(slot.isPresent());
			assertEquals(id("gem_slot"), slot.get().id());
		}

		@Test
		@DisplayName("should return empty for nonexistent slot")
		void returnsEmptyForNonexistentSlot() {
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withUpgradeSlot(upgradeSlot("gem_slot", GEM_SLOT_TYPE))
					.build();

			var slot = mutater.findSlot(pickaxeHead, id("nonexistent"));

			assertTrue(slot.isEmpty());
		}

		@Test
		@DisplayName("should not find structure parts (only mutable slots)")
		void doesNotFindStructureParts() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withStructureSlot(structureSlot("material", TOOL_MATERIAL_ID, iron))
					.build();

			var slot = mutater.findSlot(pickaxeHead, id("material"));

			assertTrue(slot.isEmpty(), "Structure parts should not be returned as Slots");
		}
	}

	@Nested
	@DisplayName("apply")
	class ApplyMutationTests {

		@Test
		@DisplayName("should apply empty mutation without changes")
		void applyEmptyMutationReturnsEquivalentComponent() {
			Component pickaxeHead = part(PICKAXE_HEAD_ID).withTag(PICKAXE_HEAD_TAG).build();
			var mutation = new ComponentMutater.Mutation.Builder().build();

			Component result = mutater.apply(pickaxeHead, mutation);

			assertEquals(pickaxeHead.id(), result.id());
		}

		@Test
		@DisplayName("should apply structure changes")
		void appliesStructureChanges() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component diamond = material(DIAMOND_ID, METAL_TAG);

			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withTag(PICKAXE_HEAD_TAG)
					.withStructureSlot(structureSlot("material", TOOL_MATERIAL_ID, iron))
					.build();

			var mutation = new ComponentMutater.Mutation.Builder()
					.withStructure(id("material"), diamond)
					.build();

			Component result = mutater.apply(pickaxeHead, mutation);

			var structured = (StructuredComponent) result;
			var part = structured.structure().getPart(id("material"));
			assertTrue(part.isPresent());
			assertEquals(diamond, part.get().getContent());
		}

		@Test
		@DisplayName("should apply upgrade changes")
		void appliesUpgradeChanges() {
			Component gem = part(GEM_ID).withTag(GEM_TAG).build();

			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withTag(PICKAXE_HEAD_TAG)
					.withUpgradeSlot(upgradeSlot("gem_slot", GEM_SLOT_TYPE))
					.build();

			var mutation = new ComponentMutater.Mutation.Builder()
					.withUpgrade(id("gem_slot"), gem)
					.build();

			Component result = mutater.apply(pickaxeHead, mutation);

			var customizable = (CustomizableComponent) result;
			var upgradeSlot = customizable.upgrades().allUpgradeSlots().get(0);
			assertTrue(upgradeSlot.getContent().isPresent());
			assertEquals(gem, upgradeSlot.getContent().get());
		}

		@Test
		@DisplayName("should apply multiple mutations in single call")
		void appliesMultipleMutations() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component diamond = material(DIAMOND_ID, METAL_TAG);
			Component gem = part(GEM_ID).withTag(GEM_TAG).build();

			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withTag(PICKAXE_HEAD_TAG)
					.withStructureSlot(structureSlot("material", TOOL_MATERIAL_ID, iron))
					.withUpgradeSlot(upgradeSlot("gem_slot", GEM_SLOT_TYPE))
					.build();

			var mutation = new ComponentMutater.Mutation.Builder()
					.withStructure(id("material"), diamond)
					.withUpgrade(id("gem_slot"), gem)
					.build();

			Component result = mutater.apply(pickaxeHead, mutation);

			// Verify structure change
			var structured = (StructuredComponent) result;
			var part = structured.structure().getPart(id("material"));
			assertEquals(diamond, part.get().getContent());

			// Verify upgrade change
			var customizable = (CustomizableComponent) result;
			var upgradeSlot = customizable.upgrades().allUpgradeSlots().get(0);
			assertEquals(gem, upgradeSlot.getContent().get());
		}
	}
}
