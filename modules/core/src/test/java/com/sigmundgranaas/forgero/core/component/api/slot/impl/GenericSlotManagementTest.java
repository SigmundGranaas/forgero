package com.sigmundgranaas.forgero.core.component.api.slot.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.core.component.impl.ExtensibleEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.mutater;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the slot manager can install, query, and remove a <em>plugin</em> slot kind — not just
 * {@link com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot} — through its
 * high-level API (change #2). Install/remove route through the now kind-generic mutater, and
 * compatibility goes through the {@link Slot#validate} seam.
 */
class GenericSlotManagementTest {

	private static OpenIdentifier id(String path) {
		return OpenIdentifier.parse("forgero:" + path);
	}

	@Test
	void installAndRemovePluginSlotKindViaSlotManager() {
		SlotManager manager = new SlotManagerImpl(mutater());
		OpenIdentifier slotId = id("vial");
		Component gem = new StaticComponent(id("ruby"), Set.of(id("gem")), Collections.emptyMap());

		Component charm = ExtensibleEquipment.create(id("charm"), Set.of(id("charm")), Collections.emptyMap(),
				ComponentUpgrades.ofSlots(List.of(new PluginSlot(slotId, id("potion_slot"), Optional.empty()))));

		// getAllSlots exposes the plugin kind (getAllUpgradeSlots would not).
		assertEquals(1, manager.getAllSlots(charm).size());
		assertEquals(PluginSlot.KIND, manager.getAllSlots(charm).get(0).type());
		assertTrue(manager.getAllUpgradeSlots(charm).isEmpty(), "plugin slot is not an upgrade slot");

		// Install through the generic high-level API.
		Component installed = manager.installInSlot(charm, slotId, gem);
		Slot filled = manager.getAllSlots(installed).get(0);
		assertEquals(PluginSlot.KIND, filled.type(), "kind preserved through install");
		assertTrue(filled.componentContent().isPresent(), "content installed");
		assertEquals(gem.id(), filled.componentContent().get().id());

		// Remove through the generic high-level API.
		Component removed = manager.removeFromSlot(installed, slotId);
		Slot emptied = manager.getAllSlots(removed).get(0);
		assertTrue(emptied.componentContent().isEmpty(), "slot emptied");
		assertEquals(PluginSlot.KIND, emptied.type(), "kind preserved through remove");
	}

	@Test
	void autoRouteInstallTargetsComponentHoldingKindAndSkipsOthers() {
		SlotManager manager = new SlotManagerImpl(mutater());
		Component gem = new StaticComponent(id("ruby"), Set.of(id("gem")), Collections.emptyMap());

		// A charm with a non-Component slot first (acceptsComponent() == false) and a plugin
		// Component slot second. Auto-routing must skip the first and install into the second.
		OpenIdentifier inertId = id("inert");
		OpenIdentifier vialId = id("vial");
		Component charm = ExtensibleEquipment.create(id("charm"), Set.of(id("charm")), Collections.emptyMap(),
				ComponentUpgrades.ofSlots(List.of(
						new NonComponentSlot(inertId),
						new PluginSlot(vialId, id("potion_slot"), Optional.empty()))));

		var result = manager.install(charm, gem);
		assertTrue(result.success(), "auto-routed install should find the Component-holding slot");
		assertEquals(vialId, result.slotId().orElseThrow(), "installed into the plugin Component slot, not the inert one");

		Component installed = result.component().orElseThrow();
		assertTrue(manager.getAllSlots(installed).stream()
				.anyMatch(s -> s.type().equals(PluginSlot.KIND) && s.componentContent().isPresent()));
		assertTrue(manager.getAllSlots(installed).stream()
				.anyMatch(s -> s.type().equals(NonComponentSlot.KIND) && s.componentContent().isEmpty()),
				"inert slot untouched");
	}

	/** A minimal Component-holding slot kind standing in for a plugin-registered slot. */
	private record PluginSlot(OpenIdentifier id, OpenIdentifier slotType, Optional<Component> content) implements Slot {
		static final OpenIdentifier KIND = OpenIdentifier.parse("forgero:test_potion_slot");

		@Override
		public OpenIdentifier type() {
			return KIND;
		}

		@Override
		public String description() {
			return "";
		}

		@Override
		public Optional<Component> componentContent() {
			return content;
		}

		@Override
		public Slot withComponentContent(Optional<Component> newContent) {
			return new PluginSlot(id, slotType, newContent);
		}

		@Override
		public boolean acceptsComponent() {
			return true;
		}
	}

	/** A non-Component slot kind (like StatusModifierSlot): must never be an auto-install target. */
	private record NonComponentSlot(OpenIdentifier id) implements Slot {
		static final OpenIdentifier KIND = OpenIdentifier.parse("forgero:test_inert_slot");

		@Override
		public OpenIdentifier type() {
			return KIND;
		}

		@Override
		public OpenIdentifier slotType() {
			return KIND;
		}

		@Override
		public String description() {
			return "";
		}
		// acceptsComponent() defaults to false; componentContent() defaults to empty.
	}
}
