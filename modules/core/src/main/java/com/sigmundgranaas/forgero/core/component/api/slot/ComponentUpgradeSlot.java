package com.sigmundgranaas.forgero.core.component.api.slot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Mutable slot that can hold a component upgrade.
 * This is one implementation of the Slot interface - plugins can add others like ArrowSlot, SoulSlot, etc.
 *
 * Can be filled, emptied, and has validation.
 * Content components contribute properties to the parent (unless filtered by conditions).
 *
 * @param id          The unique identifier for this slot.
 * @param slotType    The category of the slot, e.g., "forgero:binding", "forgero:gem". This is the
 *                    slot's install identity (what it accepts) and the thing an "in any upgrade slot"
 *                    condition matches.
 * @param description A human-readable description.
 * @param tags        The slot's identity tags — additional matchable identities beyond its type,
 *                    notably its context (e.g. {@code forgero:contexts/offensive}). An
 *                    {@code in_slot_type} condition matches the slot by its type <em>or</em> any of
 *                    these tags, so a contextual upgrade bonus can gate on the slot's context.
 * @param validator   Validation rules for content placed in this slot.
 * @param content     The component currently in the slot, or empty if not filled.
 */
public record ComponentUpgradeSlot(
		OpenIdentifier id,
		OpenIdentifier slotType,
		String description,
		Set<OpenIdentifier> tags,
		SlotValidator validator,
		Optional<Component> content
) implements Slot {

	public static final String TYPE = "forgero:component_upgrade";

	public ComponentUpgradeSlot {
		tags = tags == null ? Set.of() : Set.copyOf(tags);
		content.ifPresent(c -> validator.validate(c, id).ifPresent(error -> {
			throw new IllegalArgumentException(error);
		}));
	}

	@Override
	public OpenIdentifier type() {
		return OpenIdentifier.parse(TYPE);
	}

	/**
	 * An upgrade slot's content <em>is</em> the Component it contributes to the parent's compiled
	 * tree, so it participates in stat/property compilation through the generic {@link Slot} hook.
	 */
	@Override
	public Optional<Component> componentContent() {
		return content;
	}

	/**
	 * Overlay seam (see {@link Slot#withComponentContent}): installs the given content (or empties),
	 * preserving this slot's id, type, tags, and validator. The NBT round-trip restores installed
	 * upgrades onto the pristine slot through this.
	 */
	@Override
	public Slot withComponentContent(Optional<Component> newContent) {
		return newContent.<Slot>map(this::withContent).orElseGet(this::empty);
	}

	/** Compatibility seam (see {@link Slot#validate}): delegates to this slot's {@link SlotValidator}. */
	@Override
	public Optional<String> validate(Component content) {
		return validator.validate(content, id);
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
		return new ComponentUpgradeSlot(id, slotType, description, tags, validator, Optional.empty());
	}

	/**
	 * Returns a new slot with the given content, validating it.
	 *
	 * @param newContent The new content to place in this slot
	 * @return A new ComponentUpgradeSlot with the updated content
	 * @throws IllegalArgumentException if the new content fails validation
	 */
	public ComponentUpgradeSlot withContent(Component newContent) {
		return new ComponentUpgradeSlot(id, slotType, description, tags, validator, Optional.of(newContent));
	}

	// Static factory methods

	/**
	 * Creates an empty upgrade slot that requires content to have the slot's type as a tag.
	 */
	public static ComponentUpgradeSlot emptyOfType(OpenIdentifier id, OpenIdentifier slotType, String description) {
		return new ComponentUpgradeSlot(id, slotType, description, Set.of(), SlotValidator.requireTag(slotType), Optional.empty());
	}

	/**
	 * Creates an empty upgrade slot carrying identity tags (e.g. its context).
	 */
	public static ComponentUpgradeSlot emptyWithTags(OpenIdentifier id, OpenIdentifier slotType, String description, Set<OpenIdentifier> tags) {
		return new ComponentUpgradeSlot(id, slotType, description, tags, SlotValidator.requireTag(slotType), Optional.empty());
	}

	/**
	 * Creates an empty upgrade slot with a custom validator.
	 */
	public static ComponentUpgradeSlot emptyWithValidator(OpenIdentifier id, OpenIdentifier slotType, String description,
	                                                       SlotValidator validator) {
		return new ComponentUpgradeSlot(id, slotType, description, Set.of(), validator, Optional.empty());
	}

	/**
	 * Creates an empty upgrade slot with identity tags and a custom validator.
	 */
	public static ComponentUpgradeSlot emptyWithTagsAndValidator(OpenIdentifier id, OpenIdentifier slotType, String description,
	                                                              Set<OpenIdentifier> tags, SlotValidator validator) {
		return new ComponentUpgradeSlot(id, slotType, description, tags, validator, Optional.empty());
	}

	/**
	 * Creates a filled upgrade slot that requires content to have the slot's type as a tag.
	 */
	public static ComponentUpgradeSlot filledOfType(OpenIdentifier id, OpenIdentifier slotType, String description,
	                                                 Component content) {
		return new ComponentUpgradeSlot(id, slotType, description, Set.of(), SlotValidator.requireTag(slotType), Optional.of(content));
	}

	/**
	 * Creates a filled upgrade slot with a custom validator.
	 */
	public static ComponentUpgradeSlot filled(OpenIdentifier id, OpenIdentifier slotType, String description,
	                                           SlotValidator validator, Component content) {
		return new ComponentUpgradeSlot(id, slotType, description, Set.of(), validator, Optional.of(content));
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
			// Slot type is a tag-like classifier — preserve its full path (see CofCodecs).
			CodecConstants.TAG_IDENTIFIER_CODEC.fieldOf("slot_type").forGetter(ComponentUpgradeSlot::slotType),
			Codec.STRING.optionalFieldOf("description", "").forGetter(ComponentUpgradeSlot::description),
			Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("tags", List.of())
					.forGetter(slot -> List.copyOf(slot.tags()))
		).apply(instance, (id, slotType, description, tags) ->
			ComponentUpgradeSlot.emptyWithTags(id, slotType, description, Set.copyOf(tags))
		));
}
