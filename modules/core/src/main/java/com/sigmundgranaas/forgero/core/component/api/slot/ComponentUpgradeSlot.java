package com.sigmundgranaas.forgero.core.component.api.slot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.Optional;

/**
 * Mutable slot that can hold a component upgrade.
 * This is one implementation of the Slot interface - plugins can add others like ArrowSlot, SoulSlot, etc.
 *
 * Can be filled, emptied, and has validation.
 * Content components contribute properties to the parent (unless filtered by conditions).
 *
 * @param id          The unique identifier for this slot.
 * @param slotType    The category of the slot, e.g., "forgero:binding", "forgero:gem".
 * @param description A human-readable description.
 * @param validator   Validation rules for content placed in this slot.
 * @param content     The component currently in the slot, or empty if not filled.
 */
public record ComponentUpgradeSlot(
		OpenIdentifier id,
		OpenIdentifier slotType,
		String description,
		SlotValidator validator,
		Optional<Component> content
) implements Slot {

	public static final String TYPE = "forgero:component_upgrade";

	public ComponentUpgradeSlot {
		content.ifPresent(c -> validator.validate(c, id).ifPresent(error -> {
			throw new IllegalArgumentException(error);
		}));
	}

	@Override
	public OpenIdentifier type() {
		return OpenIdentifier.parse(TYPE);
	}

	// Implementation-specific API

	/**
	 * @return The component content of this slot, if any
	 */
	public Optional<Component> getContent() {
		return content;
	}

	/**
	 * @return The validator for this slot
	 */
	public SlotValidator validator() {
		return validator;
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
	 * Returns this slot emptied, preserving its configuration.
	 */
	public ComponentUpgradeSlot empty() {
		return new ComponentUpgradeSlot(id, slotType, description, validator, Optional.empty());
	}

	/**
	 * Returns a new slot with the given content, validating it.
	 *
	 * @param newContent The new content to place in this slot
	 * @return A new ComponentUpgradeSlot with the updated content
	 * @throws IllegalArgumentException if the new content fails validation
	 */
	public ComponentUpgradeSlot withContent(Component newContent) {
		return new ComponentUpgradeSlot(id, slotType, description, validator, Optional.of(newContent));
	}

	// Static factory methods

	/**
	 * Creates an empty upgrade slot that requires content to have the slot's type as a tag.
	 */
	public static ComponentUpgradeSlot emptyOfType(OpenIdentifier id, OpenIdentifier slotType, String description) {
		return new ComponentUpgradeSlot(id, slotType, description, SlotValidator.requireTag(slotType), Optional.empty());
	}

	/**
	 * Creates an empty upgrade slot with a custom validator.
	 */
	public static ComponentUpgradeSlot emptyWithValidator(OpenIdentifier id, OpenIdentifier slotType, String description,
	                                                       SlotValidator validator) {
		return new ComponentUpgradeSlot(id, slotType, description, validator, Optional.empty());
	}

	/**
	 * Creates a filled upgrade slot that requires content to have the slot's type as a tag.
	 */
	public static ComponentUpgradeSlot filledOfType(OpenIdentifier id, OpenIdentifier slotType, String description,
	                                                 Component content) {
		return new ComponentUpgradeSlot(id, slotType, description, SlotValidator.requireTag(slotType), Optional.of(content));
	}

	/**
	 * Creates a filled upgrade slot with a custom validator.
	 */
	public static ComponentUpgradeSlot filled(OpenIdentifier id, OpenIdentifier slotType, String description,
	                                           SlotValidator validator, Component content) {
		return new ComponentUpgradeSlot(id, slotType, description, validator, Optional.of(content));
	}

	// Codec for (de)serialization

	/**
	 * Codec for ComponentUpgradeSlot.
	 * Serializes/deserializes slot configuration.
	 * Note: This codec creates empty slots with tag-based validation.
	 * Custom validators and content are not serialized (runtime-only).
	 */
	public static final Codec<ComponentUpgradeSlot> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id").forGetter(ComponentUpgradeSlot::id),
			CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("slot_type").forGetter(ComponentUpgradeSlot::slotType),
			Codec.STRING.optionalFieldOf("description", "").forGetter(ComponentUpgradeSlot::description)
		).apply(instance, (id, slotType, description) ->
			ComponentUpgradeSlot.emptyOfType(id, slotType, description)
		));
}
