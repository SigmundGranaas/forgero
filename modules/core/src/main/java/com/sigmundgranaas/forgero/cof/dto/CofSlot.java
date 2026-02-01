package com.sigmundgranaas.forgero.cof.dto;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A unified DTO for any slot, whether in a structure or an upgrade section.
 * It's used both during the initial data pipeline build process and for runtime serialization.
 * The `id` is used to uniquely identify the slot within its parent.
 *
 * @param id          The unique identifier for this slot (e.g., "forgero:head", "forgero:gem_slot_1").
 * @param type        The type of component this slot accepts (e.g., "forgero:material", "forgero:gem").
 * @param description A human-readable description of the slot.
 * @param scope       Optional scope identifier for attribute filtering (e.g., "forgero:offensive", "forgero:defensive", "forgero:utility").
 * @param content     The full CofComponent DTO of the item currently in the slot. Null if empty.
 * @param validTags   An optional list of tags that a component must have to be valid for this slot.
 */
public record CofSlot(
		OpenIdentifier id,
		OpenIdentifier type,
		@Nullable String description,
		@Nullable OpenIdentifier scope,
		@Nullable CofComponent content,
		@Nullable List<OpenIdentifier> validTags
) {
	/**
	 * Helper for codecs that work with Optional fields.
	 */
	public Optional<CofComponent> contentOpt() {
		return Optional.ofNullable(content);
	}

	public Optional<OpenIdentifier> scopeOpt() {
		return Optional.ofNullable(scope);
	}
}
