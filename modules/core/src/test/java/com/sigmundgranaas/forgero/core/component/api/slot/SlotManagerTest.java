package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.impl.SlotManagerImpl;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.mutation.impl.ComponentMutaterImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SlotManagerTest {

	private SlotManager slotManager;
	private ComponentMutater mutater;

	// Test identifiers
	private static final OpenIdentifier TOOL_ID = id("test:diamond_sword");
	private static final OpenIdentifier GEM_ID = id("test:ruby");
	private static final OpenIdentifier BINDING_ID = id("test:leather_binding");
	private static final OpenIdentifier EMERALD_ID = id("test:emerald");

	private static final OpenIdentifier GEM_TYPE = id("forgero:gem");
	private static final OpenIdentifier BINDING_TYPE = id("forgero:binding");
	private static final OpenIdentifier GEM_SLOT_1 = id("gem_slot_1");
	private static final OpenIdentifier GEM_SLOT_2 = id("gem_slot_2");
	private static final OpenIdentifier BINDING_SLOT = id("binding_slot");

	@BeforeEach
	void setUp() {
		mutater = new ComponentMutaterImpl();
		slotManager = new SlotManagerImpl(mutater);
	}

	// ========== QUERY TESTS ==========

	@Test
	void getAllUpgradeSlots_ReturnsAllSlots() {
		Component tool = createToolWithTwoGemSlots();

		List<UpgradeSlot> slots = slotManager.getAllUpgradeSlots(tool);

		assertEquals(2, slots.size());
	}

	@Test
	void getAllUpgradeSlots_ReturnsEmptyForNonCustomizable() {
		Component gem = createGem();

		List<UpgradeSlot> slots = slotManager.getAllUpgradeSlots(gem);

		assertTrue(slots.isEmpty());
	}

	@Test
	void getEmptyUpgradeSlots_ReturnsOnlyEmpty() {
		Component tool = createToolWithMixedSlots(); // 1 filled, 1 empty

		List<UpgradeSlot> emptySlots = slotManager.getEmptyUpgradeSlots(tool);

		assertEquals(1, emptySlots.size());
		assertTrue(emptySlots.get(0).isEmpty());
	}

	@Test
	void getFilledUpgradeSlots_ReturnsOnlyFilled() {
		Component tool = createToolWithMixedSlots(); // 1 filled, 1 empty

		List<UpgradeSlot> filledSlots = slotManager.getFilledUpgradeSlots(tool);

		assertEquals(1, filledSlots.size());
		assertTrue(filledSlots.get(0).isFilled());
	}

	@Test
	void getInstalledUpgrades_ReturnsComponentsOnly() {
		Component tool = createToolWithMixedSlots();

		List<Component> upgrades = slotManager.getInstalledUpgrades(tool);

		assertEquals(1, upgrades.size());
		assertEquals(GEM_ID, upgrades.get(0).id());
	}

	// ========== COMPATIBILITY TESTS ==========

	@Test
	void canInstall_ReturnsTrueWhenCompatibleSlotExists() {
		Component tool = createToolWithTwoGemSlots();
		Component gem = createGem();

		boolean canInstall = slotManager.canInstall(tool, gem);

		assertTrue(canInstall);
	}

	@Test
	void canInstall_ReturnsFalseWhenNoCompatibleSlot() {
		Component tool = createToolWithBindingSlotOnly();
		Component gem = createGem();

		boolean canInstall = slotManager.canInstall(tool, gem);

		assertFalse(canInstall);
	}

	@Test
	void canInstall_ReturnsFalseWhenAllSlotsFilledOrIncompatible() {
		Component tool = createToolWithAllSlotsFilled();
		Component newGem = createEmerald();

		boolean canInstall = slotManager.canInstall(tool, newGem);

		assertFalse(canInstall); // Both gem slots already filled
	}

	@Test
	void findCompatibleSlot_FindsFirstMatch() {
		Component tool = createToolWithTwoGemSlots();
		Component gem = createGem();

		Optional<UpgradeSlot> slot = slotManager.findCompatibleSlot(tool, gem);

		assertTrue(slot.isPresent());
		assertEquals(GEM_SLOT_1, slot.get().id());
	}

	@Test
	void findCompatibleSlot_ReturnsEmptyWhenNoMatch() {
		Component tool = createToolWithBindingSlotOnly();
		Component gem = createGem();

		Optional<UpgradeSlot> slot = slotManager.findCompatibleSlot(tool, gem);

		assertTrue(slot.isEmpty());
	}

	@Test
	void findAllCompatibleSlots_FindsAllMatches() {
		Component tool = createToolWithTwoGemSlots();
		Component gem = createGem();

		List<UpgradeSlot> slots = slotManager.findAllCompatibleSlots(tool, gem);

		assertEquals(2, slots.size());
	}

	@Test
	void isCompatible_ReturnsTrueForValidUpgrade() {
		Component tool = createToolWithTwoGemSlots();
		UpgradeSlot slot = slotManager.getAllUpgradeSlots(tool).get(0);
		Component gem = createGem();

		boolean compatible = slotManager.isCompatible(slot, gem);

		assertTrue(compatible);
	}

	@Test
	void isCompatible_ReturnsFalseForInvalidUpgrade() {
		Component tool = createToolWithTwoGemSlots();
		UpgradeSlot slot = slotManager.getAllUpgradeSlots(tool).get(0);
		Component binding = createBinding();

		boolean compatible = slotManager.isCompatible(slot, binding);

		assertFalse(compatible);
	}

	@Test
	void validateUpgrade_ReturnsEmptyForValid() {
		Component tool = createToolWithTwoGemSlots();
		UpgradeSlot slot = slotManager.getAllUpgradeSlots(tool).get(0);
		Component gem = createGem();

		Optional<String> error = slotManager.validateUpgrade(slot, gem);

		assertTrue(error.isEmpty());
	}

	@Test
	void validateUpgrade_ReturnsErrorForInvalid() {
		Component tool = createToolWithTwoGemSlots();
		UpgradeSlot slot = slotManager.getAllUpgradeSlots(tool).get(0);
		Component binding = createBinding();

		Optional<String> error = slotManager.validateUpgrade(slot, binding);

		assertTrue(error.isPresent());
		assertTrue(error.get().contains("missing required tag"));
	}

	// ========== INSTALLATION TESTS ==========

	@Test
	void install_ReturnsSuccessWhenCompatibleSlotExists() {
		Component tool = createToolWithTwoGemSlots();
		Component gem = createGem();

		InstallationResult result = slotManager.install(tool, gem);

		assertTrue(result.success());
		assertTrue(result.component().isPresent());
		assertEquals(GEM_SLOT_1, result.slotId().orElse(null));
	}

	@Test
	void install_ReturnsFailureWhenNoCompatibleSlot() {
		Component tool = createToolWithBindingSlotOnly();
		Component gem = createGem();

		InstallationResult result = slotManager.install(tool, gem);

		assertFalse(result.success());
		assertTrue(result.component().isEmpty());
		assertTrue(result.errorMessage().isPresent());
	}

	@Test
	void install_FillsFirstCompatibleSlot() {
		Component tool = createToolWithTwoGemSlots();
		Component gem = createGem();

		InstallationResult result = slotManager.install(tool, gem);
		Component upgraded = result.orElseThrow();

		// First slot should be filled
		List<UpgradeSlot> filledSlots = slotManager.getFilledUpgradeSlots(upgraded);
		assertEquals(1, filledSlots.size());
		assertEquals(GEM_SLOT_1, filledSlots.get(0).id());

		// Second slot should be empty
		List<UpgradeSlot> emptySlots = slotManager.getEmptyUpgradeSlots(upgraded);
		assertEquals(1, emptySlots.size());
		assertEquals(GEM_SLOT_2, emptySlots.get(0).id());
	}

	@Test
	void install_ThrowsExceptionForNullTarget() {
		Component gem = createGem();

		assertThrows(IllegalArgumentException.class, () ->
			slotManager.install(null, gem)
		);
	}

	@Test
	void install_ThrowsExceptionForNullUpgrade() {
		Component tool = createToolWithTwoGemSlots();

		assertThrows(IllegalArgumentException.class, () ->
			slotManager.install(tool, null)
		);
	}

	@Test
	void installInSlot_InstallsInSpecificSlot() {
		Component tool = createToolWithTwoGemSlots();
		Component gem = createGem();

		Component upgraded = slotManager.installInSlot(tool, GEM_SLOT_2, gem);

		// Second slot should be filled
		UpgradeSlot slot = ((CustomizableComponent) upgraded).upgrades().get(GEM_SLOT_2).orElseThrow();
		assertTrue(slot.isFilled());
		assertEquals(GEM_ID, slot.content().orElseThrow().id());
	}

	@Test
	void installInSlot_ThrowsWhenSlotNotFound() {
		Component tool = createToolWithTwoGemSlots();
		Component gem = createGem();
		OpenIdentifier invalidSlot = id("invalid_slot");

		assertThrows(IllegalArgumentException.class, () ->
			slotManager.installInSlot(tool, invalidSlot, gem)
		);
	}

	@Test
	void installInSlot_ThrowsWhenSlotAlreadyFilled() {
		Component tool = createToolWithMixedSlots(); // gem_slot_1 is filled
		Component gem = createEmerald();

		assertThrows(IllegalArgumentException.class, () ->
			slotManager.installInSlot(tool, GEM_SLOT_1, gem)
		);
	}

	@Test
	void installInSlot_ThrowsWhenUpgradeIncompatible() {
		Component tool = createToolWithTwoGemSlots();
		Component binding = createBinding();

		assertThrows(IllegalArgumentException.class, () ->
			slotManager.installInSlot(tool, GEM_SLOT_1, binding)
		);
	}

	@Test
	void installOrReplace_ReplacesExistingContent() {
		Component tool = createToolWithMixedSlots(); // gem_slot_1 filled with ruby
		Component emerald = createEmerald();

		Component upgraded = slotManager.installOrReplace(tool, GEM_SLOT_1, emerald);

		// Slot should now contain emerald
		UpgradeSlot slot = ((CustomizableComponent) upgraded).upgrades().get(GEM_SLOT_1).orElseThrow();
		assertTrue(slot.isFilled());
		assertEquals(EMERALD_ID, slot.content().orElseThrow().id());
	}

	// ========== REMOVAL TESTS ==========

	@Test
	void removeUpgrade_RemovesById() {
		Component tool = createToolWithMixedSlots();

		Component updated = slotManager.removeUpgrade(tool, GEM_ID);

		assertEquals(0, slotManager.countFilledSlots(updated));
		assertEquals(2, slotManager.countEmptySlots(updated));
	}

	@Test
	void removeUpgrade_ReturnsUnchangedWhenNotFound() {
		Component tool = createToolWithMixedSlots();
		OpenIdentifier nonExistentId = id("test:non_existent");

		Component updated = slotManager.removeUpgrade(tool, nonExistentId);

		assertEquals(tool, updated); // Should be same instance
		assertEquals(1, slotManager.countFilledSlots(updated));
	}

	@Test
	void removeFromSlot_EmptiesSlot() {
		Component tool = createToolWithMixedSlots();

		Component updated = slotManager.removeFromSlot(tool, GEM_SLOT_1);

		UpgradeSlot slot = ((CustomizableComponent) updated).upgrades().get(GEM_SLOT_1).orElseThrow();
		assertTrue(slot.isEmpty());
	}

	@Test
	void removeAllUpgrades_EmptiesAllSlots() {
		Component tool = createToolWithAllSlotsFilled();

		Component stripped = slotManager.removeAllUpgrades(tool);

		assertEquals(0, slotManager.countFilledSlots(stripped));
		assertEquals(2, slotManager.countUpgradeSlots(stripped));
	}

	// ========== UTILITY TESTS ==========

	@Test
	void countUpgradeSlots_CountsCorrectly() {
		Component tool = createToolWithTwoGemSlots();

		assertEquals(2, slotManager.countUpgradeSlots(tool));
	}

	@Test
	void countFilledSlots_CountsCorrectly() {
		Component tool = createToolWithMixedSlots();

		assertEquals(1, slotManager.countFilledSlots(tool));
	}

	@Test
	void countEmptySlots_CountsCorrectly() {
		Component tool = createToolWithMixedSlots();

		assertEquals(1, slotManager.countEmptySlots(tool));
	}

	@Test
	void areAllSlotsFilled_ReturnsTrueWhenFull() {
		Component tool = createToolWithAllSlotsFilled();

		assertTrue(slotManager.areAllSlotsFilled(tool));
	}

	@Test
	void areAllSlotsFilled_ReturnsFalseWhenNotFull() {
		Component tool = createToolWithMixedSlots();

		assertFalse(slotManager.areAllSlotsFilled(tool));
	}

	@Test
	void hasUpgradeSlots_ReturnsTrueWhenPresent() {
		Component tool = createToolWithTwoGemSlots();

		assertTrue(slotManager.hasUpgradeSlots(tool));
	}

	@Test
	void hasUpgradeSlots_ReturnsFalseWhenAbsent() {
		Component gem = createGem();

		assertFalse(slotManager.hasUpgradeSlots(gem));
	}

	// ========== QUERY BUILDER TESTS ==========

	@Test
	void queryUpgradeSlots_ReturnsQueryBuilder() {
		Component tool = createToolWithTwoGemSlots();

		SlotQuery<UpgradeSlot> query = slotManager.queryUpgradeSlots(tool);

		assertNotNull(query);
		assertEquals(2, query.count());
	}

	// ========== HELPER METHODS ==========

	private static OpenIdentifier id(String id) {
		String[] parts = id.split(":");
		return new OpenIdentifier(parts[0], parts[1]);
	}

	private Component createGem() {
		return new TestComponent(GEM_ID, Set.of(GEM_TYPE), Map.of(), ComponentUpgrades.empty());
	}

	private Component createEmerald() {
		return new TestComponent(EMERALD_ID, Set.of(GEM_TYPE), Map.of(), ComponentUpgrades.empty());
	}

	private Component createBinding() {
		return new TestComponent(BINDING_ID, Set.of(BINDING_TYPE), Map.of(), ComponentUpgrades.empty());
	}

	private Component createToolWithTwoGemSlots() {
		return new TestComponent(
			TOOL_ID,
			Set.of(id("forgero:tool")),
			Map.of(),
			ComponentUpgrades.of(
				UpgradeSlot.emptyOfType(GEM_SLOT_1, GEM_TYPE, "Gem Slot 1"),
				UpgradeSlot.emptyOfType(GEM_SLOT_2, GEM_TYPE, "Gem Slot 2")
			)
		);
	}

	private Component createToolWithBindingSlotOnly() {
		return new TestComponent(
			TOOL_ID,
			Set.of(id("forgero:tool")),
			Map.of(),
			ComponentUpgrades.of(
				UpgradeSlot.emptyOfType(BINDING_SLOT, BINDING_TYPE, "Binding Slot")
			)
		);
	}

	private Component createToolWithMixedSlots() {
		Component gem = createGem();
		return new TestComponent(
			TOOL_ID,
			Set.of(id("forgero:tool")),
			Map.of(),
			ComponentUpgrades.of(
				UpgradeSlot.filledOfType(GEM_SLOT_1, GEM_TYPE, "Gem Slot 1", gem),
				UpgradeSlot.emptyOfType(GEM_SLOT_2, GEM_TYPE, "Gem Slot 2")
			)
		);
	}

	private Component createToolWithAllSlotsFilled() {
		Component ruby = createGem();
		Component emerald = createEmerald();
		return new TestComponent(
			TOOL_ID,
			Set.of(id("forgero:tool")),
			Map.of(),
			ComponentUpgrades.of(
				UpgradeSlot.filledOfType(GEM_SLOT_1, GEM_TYPE, "Gem Slot 1", ruby),
				UpgradeSlot.filledOfType(GEM_SLOT_2, GEM_TYPE, "Gem Slot 2", emerald)
			)
		);
	}

	// Test component implementation
	record TestComponent(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		Map<String, List<?>> properties,
		ComponentUpgrades upgrades
	) implements CustomizableComponent {

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return OpenIdentifier.of("forgero:test_component");
		}

		@Override
		public Set<OpenIdentifier> getTags() {
			return tags;
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return properties;
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return new TestComponent(id, tags, newProperties, upgrades);
		}

		@Override
		public Component withUpgrades(ComponentUpgrades newUpgrades) {
			return new TestComponent(id, tags, properties, newUpgrades);
		}

		@Override
		public List<Component> getChildren() {
			return upgrades.filledContents();
		}
	}
}
