package com.sigmundgranaas.forgero.core.component.api.structure;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;

import java.util.Optional;

/**
 * A required slot that must always contain a component.
 * Validation is performed at construction time.
 *
 * @param id          The unique identifier for this slot.
 * @param type        The category of the slot, e.g., "forgero:pickaxe_head", "forgero:handle".
 * @param description A human-readable description.
 * @param validator   Validation rules for content placed in this slot.
 * @param content     The actual component filling this slot. Cannot be null.
 */
public record StructureSlot(
		OpenIdentifier id,
		OpenIdentifier type,
		String description,
		SlotValidator validator,
		Component content
) implements Slot {

	public StructureSlot {
		if (content == null) {
			throw new IllegalArgumentException("StructureSlot content cannot be null for slot: " + id);
		}
		validator.validate(content, id).ifPresent(error -> {
			throw new IllegalArgumentException(error);
		});
	}

	/**
	 * Creates a structure slot that requires content to have the slot's type as a tag.
	 * This is the most common pattern.
	 */
	public static StructureSlot ofType(OpenIdentifier id, OpenIdentifier type, String description, Component content) {
		return new StructureSlot(id, type, description, SlotValidator.requireTag(type), content);
	}

	/**
	 * Creates a structure slot with custom validation.
	 */
	public static StructureSlot withValidator(OpenIdentifier id, OpenIdentifier type, String description,
	                                          SlotValidator validator, Component content) {
		return new StructureSlot(id, type, description, validator, content);
	}

	@Override
	public Optional<Component> get() {
		return Optional.of(content);
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	/**
	 * Returns a new slot with different content, validating the new content.
	 *
	 * @param newContent The new content to place in this slot
	 * @return A new StructureSlot with the updated content
	 * @throws IllegalArgumentException if the new content fails validation
	 */
	public StructureSlot withContent(Component newContent) {
		return new StructureSlot(id, type, description, validator, newContent);
	}
}
