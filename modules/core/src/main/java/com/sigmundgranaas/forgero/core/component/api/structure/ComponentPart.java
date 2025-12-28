package com.sigmundgranaas.forgero.core.component.api.structure;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;

/**
 * Immutable component part - NOT a slot.
 * Represents required parts of component structure (blade, handle, limb, etc.).
 * Cannot be removed or left empty - always contains a component.
 *
 * This is fundamentally different from mutable Slots which can be added/removed.
 * ComponentPart is used for immutable component composition.
 *
 * @param id          The unique identifier for this part within the component structure.
 * @param partType    The category of this part, e.g., "forgero:blade", "forgero:handle", "forgero:bow_limb".
 * @param description A human-readable description.
 * @param validator   Validation rules for content placed in this part.
 * @param content     The actual component filling this part. Cannot be null.
 */
public record ComponentPart(
		OpenIdentifier id,
		OpenIdentifier partType,
		String description,
		SlotValidator validator,
		Component content
) {

	public ComponentPart {
		if (content == null) {
			throw new IllegalArgumentException("ComponentPart content cannot be null for part: " + id);
		}
		validator.validate(content, id).ifPresent(error -> {
			throw new IllegalArgumentException(error);
		});
	}

	/**
	 * Creates a component part that requires content to have the part's type as a tag.
	 * This is the most common pattern.
	 */
	public static ComponentPart ofType(OpenIdentifier id, OpenIdentifier partType, String description, Component content) {
		return new ComponentPart(id, partType, description, SlotValidator.requireTag(partType), content);
	}

	/**
	 * Creates a component part with custom validation.
	 */
	public static ComponentPart withValidator(OpenIdentifier id, OpenIdentifier partType, String description,
	                                          SlotValidator validator, Component content) {
		return new ComponentPart(id, partType, description, validator, content);
	}

	/**
	 * @return The component content of this part (never null)
	 */
	public Component getContent() {
		return content;
	}

	/**
	 * Returns a new part with different content, validating the new content.
	 *
	 * @param newContent The new content to place in this part
	 * @return A new ComponentPart with the updated content
	 * @throws IllegalArgumentException if the new content fails validation
	 */
	public ComponentPart withContent(Component newContent) {
		return new ComponentPart(id, partType, description, validator, newContent);
	}
}
