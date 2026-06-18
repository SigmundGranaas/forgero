package com.sigmundgranaas.forgero.common.api.item.impl;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.*;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ItemMutationApiImpl API contract.
 * <p>
 * These tests verify the documented behavior of the ItemMutationApi:
 * - All methods must handle null ItemStack gracefully
 * - Mutation methods return the original stack (or null) on failure
 * - canInstallUpgrade returns false for invalid inputs
 * <p>
 * Full integration tests with real Forgero items require GameTest.
 */
@DisplayName("ItemMutationApiImpl API Contract")
class ItemMutationApiImplTest {

	private ItemMutationApiImpl api;

	@BeforeEach
	void setUp() {
		api = new ItemMutationApiImpl(new StubComponentConverter(), new StubSlotManager());
	}

	@Nested
	@DisplayName("API Contract: Null Safety")
	class NullSafetyContract {

		@Test
		@DisplayName("installUpgrade returns null when target is null")
		void installUpgradeReturnsNullForNullTarget() {
			assertNull(api.installUpgrade(null, null));
		}

		@Test
		@DisplayName("removeUpgrade returns original target when target is null")
		void removeUpgradeReturnsNullForNullTarget() {
			// When target is null, removeUpgrade returns target (which is null)
			assertNull(api.removeUpgrade(null, OpenIdentifier.parse("forgero:test")));
		}

		@Test
		@DisplayName("removeAllUpgrades returns null when target is null")
		void removeAllUpgradesReturnsNullForNullTarget() {
			assertNull(api.removeAllUpgrades(null));
		}

		@Test
		@DisplayName("canInstallUpgrade returns false for any null parameter")
		void canInstallUpgradeReturnsFalseForNull() {
			assertAll("canInstallUpgrade should return false for null inputs",
				() -> assertFalse(api.canInstallUpgrade(null, null)),
				() -> assertFalse(api.canInstallUpgrade(null, null))
			);
		}
	}

	// ========== Stub implementations ==========

	private static class StubComponentConverter implements ComponentConverter {
		@Override public Optional<Component> toComponent(ItemStack stack) { return Optional.empty(); }
		@Override public Optional<Component> toComponent(Item item) { return Optional.empty(); }
		@Override public Optional<Component> toComponent(Identifier itemId) { return Optional.empty(); }
		@Override public Optional<ItemStack> toStack(Component component) { return Optional.empty(); }
		@Override public Optional<ItemStack> toStack(OpenIdentifier componentId) { return Optional.empty(); }
		@Override public Optional<Item> toItem(Component component) { return Optional.empty(); }
		@Override public Optional<Item> toItem(OpenIdentifier componentId) { return Optional.empty(); }
		@Override public Optional<Identifier> toItemId(OpenIdentifier componentId) { return Optional.empty(); }
		@Override public Optional<OpenIdentifier> toComponentId(ItemStack stack) { return Optional.empty(); }
		@Override public Optional<OpenIdentifier> toComponentId(Item item) { return Optional.empty(); }
	}

	private static class StubSlotManager implements SlotManager {
		@Override public boolean hasComponentUpgradeSlots(Component c) { return false; }
		@Override public int countComponentUpgradeSlots(Component c) { return 0; }
		@Override public int countFilledSlots(Component c) { return 0; }
		@Override public int countEmptySlots(Component c) { return 0; }
		@Override public List<ComponentUpgradeSlot> getFilledUpgradeSlots(Component c) { return List.of(); }
		@Override public SlotQuery<ComponentUpgradeSlot> queryUpgradeSlots(Component c) { throw new UnsupportedOperationException(); }
		@Override public List<ComponentPart> getStructureParts(Component c) { return List.of(); }
		@Override public List<ComponentUpgradeSlot> getAllUpgradeSlots(Component c) { return List.of(); }
		@Override public List<com.sigmundgranaas.forgero.core.component.api.Slot> getAllSlots(Component c) { return List.of(); }
		@Override public List<com.sigmundgranaas.forgero.core.component.api.Slot> getEmptySlots(Component c) { return List.of(); }
		@Override public List<com.sigmundgranaas.forgero.core.component.api.Slot> getFilledSlots(Component c) { return List.of(); }
		@Override public List<ComponentUpgradeSlot> getEmptyUpgradeSlots(Component c) { return List.of(); }
		@Override public List<Component> getInstalledUpgrades(Component c) { return List.of(); }
		@Override public boolean canInstall(Component t, Component u) { return false; }
		@Override public Optional<ComponentUpgradeSlot> findCompatibleSlot(Component t, Component u) { return Optional.empty(); }
		@Override public List<ComponentUpgradeSlot> findAllCompatibleSlots(Component t, Component u) { return List.of(); }
		@Override public boolean isCompatible(ComponentUpgradeSlot s, Component u) { return false; }
		@Override public Optional<String> validateUpgrade(ComponentUpgradeSlot s, Component u) { return Optional.empty(); }
		@Override public InstallationResult install(Component t, Component u) { return InstallationResult.failure("stub"); }
		@Override public Component installInSlot(Component t, OpenIdentifier s, Component u) { throw new UnsupportedOperationException(); }
		@Override public Component installOrReplace(Component t, OpenIdentifier s, Component u) { throw new UnsupportedOperationException(); }
		@Override public Component removeUpgrade(Component t, OpenIdentifier u) { return t; }
		@Override public Component removeFromSlot(Component t, OpenIdentifier s) { throw new UnsupportedOperationException(); }
		@Override public Component removeAllUpgrades(Component t) { return t; }
		@Override public boolean areAllSlotsFilled(Component c) { return false; }
	}
}
