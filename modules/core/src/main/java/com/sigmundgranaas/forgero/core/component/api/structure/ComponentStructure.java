package com.sigmundgranaas.forgero.core.component.api.structure;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Represents the defined, required structural composition of a component.
 * This class is an immutable blueprint of a component's structure.
 *
 * @param slots A map of required slots where keys are unique slot identifiers and values are the StructureSlot objects.
 */
public record ComponentStructure(Map<OpenIdentifier, StructureSlot> slots) {
	public ComponentStructure {
		// Validate that the ID of each StructureSlot in the map's values matches its corresponding key.
		// This ensures consistency between the map key and the slot's internal ID.
		slots.forEach((id, slot) -> {
			if (slot == null) {
				throw new IllegalArgumentException("StructureSlot cannot be null for ID: " + id);
			}
			if (!slot.id().equals(id)) {
				throw new IllegalArgumentException(String.format("StructureSlot ID mismatch: Map key '%s' does not match slot's ID '%s'", id, slot.id()));
			}
		});
	}

	/**
	 * @return A list of the direct sub-components held within the structure slots.
	 */
	public List<Component> children() {
		// Iterate over the values of the map
		return slots.values().stream()
				.map(StructureSlot::content)
				.collect(Collectors.toList());
	}
}
