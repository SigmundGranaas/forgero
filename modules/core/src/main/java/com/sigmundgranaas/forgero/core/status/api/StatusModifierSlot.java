package com.sigmundgranaas.forgero.core.status.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A mutable slot that holds a status modifier.
 * Implements the Slot interface to integrate with the slot-based architecture.
 *
 * Properties from the contained StatusModifier are contributed during property resolution.
 * This slot type is on par with ComponentUpgradeSlot as a first-class citizen.
 *
 * @param id      Unique identifier for this slot within its parent component
 * @param index   Position index when multiple status modifier slots exist
 * @param content The status modifier currently in the slot, or empty if not filled
 */
public record StatusModifierSlot(
		OpenIdentifier id,
		int index,
		Optional<StatusModifier> content
) implements Slot {

	public static final String TYPE = "forgero:status_modifier";
	public static final OpenIdentifier SLOT_TYPE = OpenIdentifier.of("status_modifier");

	@Override
	public OpenIdentifier type() {
		return OpenIdentifier.parse(TYPE);
	}

	@Override
	public OpenIdentifier slotType() {
		return SLOT_TYPE;
	}

	@Override
	public String description() {
		return content
				.map(m -> "Status: " + m.displayName())
				.orElse("Empty status modifier slot");
	}

	/**
	 * Controls property contribution during resolution.
	 * Default behavior passes through all properties from the status modifier.
	 * Specific modifiers (e.g., "broken") can implement custom filtering.
	 */
	@Override
	public <P extends Property> Stream<P> filterProperties(
			PropertyKey<P> propertyKey,
			Stream<P> properties
	) {
		return properties; // Default: pass through all properties
	}

	/**
	 * Only include in property traversal if the slot contains a modifier.
	 */
	@Override
	public boolean includeInTraversal() {
		return content.isPresent();
	}

	// Slot state queries

	/**
	 * @return true if the slot currently has a modifier
	 */
	public boolean isFilled() {
		return content.isPresent();
	}

	/**
	 * @return true if the slot is currently empty
	 */
	public boolean isEmpty() {
		return content.isEmpty();
	}

	/**
	 * @return The modifier in this slot, if present
	 */
	public Optional<StatusModifier> getModifier() {
		return content;
	}

	// Immutable transformation methods

	/**
	 * Returns this slot emptied, preserving its configuration.
	 */
	public StatusModifierSlot empty() {
		return new StatusModifierSlot(id, index, Optional.empty());
	}

	/**
	 * Returns a new slot with the given modifier installed.
	 *
	 * @param modifier The modifier to install
	 * @return A new StatusModifierSlot with the modifier
	 */
	public StatusModifierSlot withModifier(StatusModifier modifier) {
		return new StatusModifierSlot(id, index, Optional.of(modifier));
	}

	// Static factory methods

	/**
	 * Creates an empty status modifier slot.
	 *
	 * @param id    Unique identifier for this slot
	 * @param index Position index for ordering
	 * @return An empty StatusModifierSlot
	 */
	public static StatusModifierSlot empty(OpenIdentifier id, int index) {
		return new StatusModifierSlot(id, index, Optional.empty());
	}

	/**
	 * Creates a filled status modifier slot.
	 *
	 * @param id       Unique identifier for this slot
	 * @param index    Position index for ordering
	 * @param modifier The modifier to install
	 * @return A filled StatusModifierSlot
	 */
	public static StatusModifierSlot filled(OpenIdentifier id, int index, StatusModifier modifier) {
		return new StatusModifierSlot(id, index, Optional.of(modifier));
	}

	/**
	 * Creates an empty slot with auto-generated ID based on index.
	 *
	 * @param index Position index
	 * @return An empty StatusModifierSlot with generated ID
	 */
	public static StatusModifierSlot emptyAt(int index) {
		return new StatusModifierSlot(
				OpenIdentifier.of("status_slot_" + index),
				index,
				Optional.empty()
		);
	}

	// Codec for (de)serialization

	/**
	 * Codec for StatusModifierSlot.
	 * Serializes slot configuration (id, index).
	 * Content (the modifier) is resolved at runtime from StatusModifierRegistry.
	 *
	 * JSON format:
	 * {
	 *   "type": "forgero:status_modifier",
	 *   "id": "status_slot_0",
	 *   "index": 0
	 * }
	 */
	public static final Codec<StatusModifierSlot> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id").forGetter(StatusModifierSlot::id),
					Codec.INT.optionalFieldOf("index", 0).forGetter(StatusModifierSlot::index)
			).apply(instance, (id, index) -> StatusModifierSlot.empty(id, index))
	);
}
