package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;

import java.util.stream.Stream;

/**
 * Base interface for MUTABLE slot types.
 * Slots are containers that can be added to or removed from components.
 * Each slot implementation defines its own content type, validation, and behavior.
 *
 * NOTE: This is for MUTABLE slots only. Immutable component structure uses ComponentPart, not Slot.
 *
 * Implementations include:
 * - ComponentUpgradeSlot: Holds component upgrades (gems, bindings, etc.)
 * - ArrowSlot: Holds arrow items for bows (plugin example)
 * - SoulSlot: Holds soul data for stat tracking (plugin example)
 */
public interface Slot {
	/**
	 * @return Unique identifier for this slot within its parent component
	 */
	OpenIdentifier id();

	/**
	 * @return Slot implementation type for codec dispatch
	 * Examples: "forgero:component_upgrade", "forgero:arrow", "forgero:soul"
	 */
	OpenIdentifier type();

	/**
	 * @return Slot category/sub-type (e.g., "forgero:gem", "forgero:binding", "forgero:arrow")
	 * This is the semantic type of what the slot holds.
	 */
	OpenIdentifier slotType();

	/**
	 * @return Human-readable description
	 */
	String description();

	/**
	 * Controls property contribution during resolution.
	 * Called once per PropertyKey during property bake phase.
	 *
	 * Implementations can filter or transform properties from their content.
	 * For example, ArrowSlot might return an empty stream to prevent arrows from contributing properties.
	 *
	 * @param propertyKey The property type being resolved
	 * @param properties Properties from this slot's content (if applicable)
	 * @return Filtered properties that should contribute to parent
	 */
	default <P extends Property> Stream<P> filterProperties(
		PropertyKey<P> propertyKey,
		Stream<P> properties
	) {
		return properties;  // Default: pass through all properties
	}

	/**
	 * Controls component tree traversal during property resolution.
	 *
	 * @return true if this slot's content should be included in property resolution traversal
	 */
	default boolean includeInTraversal() {
		return true;  // Default: include in traversal
	}
}
