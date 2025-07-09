package com.sigmundgranaas.forgero.core.component.api.structure;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the defined, required structural composition of a component.
 * This class is an immutable blueprint of a component's structure.
 *
 * @param slots The list of required slots that form the structure.
 */
public record ComponentStructure(List<StructureSlot> slots) {
	public ComponentStructure {
		// Validate that all slot IDs are unique within this component's structure.
		var uniqueIds = slots.stream().map(Slot::id).collect(Collectors.toSet());
		if (uniqueIds.size() != slots.size()) {
			throw new IllegalArgumentException("Duplicate structure slot IDs found in component.");
		}
	}

	/**
	 * @return A list of the direct sub-components held within the structure slots.
	 */
	public List<Component> children() {
		return slots.stream()
				.map(StructureSlot::content)
				.collect(Collectors.toList());
	}
}
