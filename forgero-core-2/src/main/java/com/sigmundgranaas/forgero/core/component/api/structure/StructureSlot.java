package com.sigmundgranaas.forgero.core.component.api.structure;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import java.util.Optional;

/**
 * Describes a required, defining part of a structure.
 *
 * @param id          The unique identifier for this slot.
 * @param type        The category of the slot, e.g., "forgero:pickaxe_head", "forgero:handle".
 * @param description A human-readable description.
 * @param content     The actual component filling this slot. Cannot be empty.
 */
public record StructureSlot(
		OpenIdentifier id,
		OpenIdentifier type,
		String description,
		Component content
) implements Slot {
	@Override
	public Optional<Component> get() {
		return Optional.of(content);
	}

	@Override
	public boolean isRequired() {
		return true;
	}
}
