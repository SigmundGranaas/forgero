package com.sigmundgranaas.forgero.core.component.slot;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Describes an optional slot for adding upgrades to a component.
 * This record represents the state of a slot on a specific item instance,
 * meaning it can be either filled or empty.
 *
 * @param id          The unique identifier for this slot.
 * @param type        The category of the slot, e.g., "forgero:binding", "forgero:gem".
 * @param description A human-readable description.
 * @param validator   Logic to check if a component is valid for this slot.
 * @param content     The component currently in the slot, or empty if it's not filled.
 */
public record UpgradeSlot(
		OpenIdentifier id,
		OpenIdentifier type,
		String description,
		Predicate<Component> validator,
		Optional<Component> content
) implements Slot {

	@Override
	public Optional<Component> get() {
		return content;
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	public UpgradeSlot empty() {
		return new UpgradeSlot(id, type, description, validator, Optional.empty());
	}

	public UpgradeSlot apply(Component newContent) {
		if (!validator.test(newContent)) {
			throw new IllegalArgumentException(String.format("Component %s is not valid for slot %s", newContent.id(), id));
		}
		return new UpgradeSlot(id, type, description, validator, Optional.of(newContent));
	}
}
