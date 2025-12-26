package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;

import java.util.Optional;

/**
 * An optional slot that may or may not contain a component.
 * Validation is performed at construction time when content is present.
 *
 * @param id          The unique identifier for this slot.
 * @param type        The category of the slot, e.g., "forgero:binding", "forgero:gem".
 * @param description A human-readable description.
 * @param validator   Validation rules for content placed in this slot.
 * @param content     The component currently in the slot, or empty if not filled.
 */
public record UpgradeSlot(
		OpenIdentifier id,
		OpenIdentifier type,
		String description,
		SlotValidator validator,
		Optional<Component> content
) implements Slot {

	public UpgradeSlot {
		content.ifPresent(c -> validator.validate(c, id).ifPresent(error -> {
			throw new IllegalArgumentException(error);
		}));
	}

	/**
	 * Creates an empty upgrade slot that requires content to have the slot's type as a tag.
	 */
	public static UpgradeSlot emptyOfType(OpenIdentifier id, OpenIdentifier type, String description) {
		return new UpgradeSlot(id, type, description, SlotValidator.requireTag(type), Optional.empty());
	}

	/**
	 * Creates an empty upgrade slot with a custom validator.
	 */
	public static UpgradeSlot emptyWithValidator(OpenIdentifier id, OpenIdentifier type, String description,
	                                              SlotValidator validator) {
		return new UpgradeSlot(id, type, description, validator, Optional.empty());
	}

	/**
	 * Creates a filled upgrade slot that requires content to have the slot's type as a tag.
	 */
	public static UpgradeSlot filledOfType(OpenIdentifier id, OpenIdentifier type, String description,
	                                        Component content) {
		return new UpgradeSlot(id, type, description, SlotValidator.requireTag(type), Optional.of(content));
	}

	/**
	 * Creates a filled upgrade slot with a custom validator.
	 */
	public static UpgradeSlot filled(OpenIdentifier id, OpenIdentifier type, String description,
	                                  SlotValidator validator, Component content) {
		return new UpgradeSlot(id, type, description, validator, Optional.of(content));
	}

	@Override
	public Optional<Component> get() {
		return content;
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	/**
	 * Returns this slot emptied, preserving its configuration.
	 */
	public UpgradeSlot empty() {
		return new UpgradeSlot(id, type, description, validator, Optional.empty());
	}

	/**
	 * Returns a new slot with the given content, validating it.
	 *
	 * @param newContent The new content to place in this slot
	 * @return A new UpgradeSlot with the updated content
	 * @throws IllegalArgumentException if the new content fails validation
	 */
	public UpgradeSlot withContent(Component newContent) {
		return new UpgradeSlot(id, type, description, validator, Optional.of(newContent));
	}

	/**
	 * Checks if the slot currently has content.
	 */
	public boolean isFilled() {
		return content.isPresent();
	}

	/**
	 * Checks if the slot is currently empty.
	 */
	public boolean isEmpty() {
		return content.isEmpty();
	}

	/**
	 * Legacy compatibility: applies content using the validator.
	 *
	 * @deprecated Use {@link #withContent(Component)} instead
	 */
	@Deprecated
	public UpgradeSlot apply(Component newContent) {
		return withContent(newContent);
	}
}
