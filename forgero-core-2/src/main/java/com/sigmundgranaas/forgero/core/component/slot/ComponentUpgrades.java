package com.sigmundgranaas.forgero.core.component.slot;

import com.sigmundgranaas.forgero.core.component.api.Slot;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the available upgrade slots on a component.
 * This class is an immutable blueprint of a component's customization points.
 *
 * @param slots The list of available upgrade slots.
 */
public record ComponentUpgrades(List<UpgradeSlot> slots) {
	public ComponentUpgrades {
		// Validate that all slot IDs are unique within this component's upgrades.
		var uniqueIds = slots.stream().map(Slot::id).collect(Collectors.toSet());
		if (uniqueIds.size() != slots.size()) {
			throw new IllegalArgumentException("Duplicate upgrade slot IDs found in component.");
		}
	}
}
