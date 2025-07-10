package com.sigmundgranaas.forgero.cof.dto;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Optional;

/**
 * DTO for a serialized UpgradeSlot. The validator is not serialized as it's part of the pristine definition.
 * The 'content' is an Optional CofComponent, which will be serialized recursively.
 *
 * @param id The unique identifier for this slot.
 * @param type The category of the slot.
 * @param description A human-readable description.
 * @param content The serialized component in the slot, if any.
 */
public record CofUpgradeSlot(
		OpenIdentifier id,
		OpenIdentifier type,
		String description,
		Optional<CofComponent> content
) {
}
