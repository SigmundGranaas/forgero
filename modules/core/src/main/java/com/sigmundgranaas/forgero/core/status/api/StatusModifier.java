package com.sigmundgranaas.forgero.core.status.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.PropertyHolder;

import java.util.Set;

/**
 * A status modifier represents a named condition that can be applied to components.
 * Examples: "sharp", "durable", "broken", "unbreakable"
 *
 * Status modifiers contribute properties when installed in a StatusModifierSlot.
 * They are the spiritual successor to the old ConditionedState system.
 *
 * @see StatusModifierSlot
 * @see StatusModifierRegistry
 */
public interface StatusModifier extends PropertyHolder {

	/**
	 * @return Unique identifier for this modifier (e.g., "forgero:sharp")
	 */
	OpenIdentifier id();

	/**
	 * @return Display name for UI purposes (e.g., "Sharp")
	 */
	String displayName();

	/**
	 * @return Priority for property resolution ordering (higher = applied first)
	 */
	int priority();

	/**
	 * @return Set of modifier IDs that cannot coexist with this modifier
	 */
	Set<OpenIdentifier> incompatibilities();

	/**
	 * Checks if this modifier is incompatible with another.
	 *
	 * @param other The other modifier to check
	 * @return true if the modifiers cannot coexist
	 */
	default boolean isIncompatibleWith(StatusModifier other) {
		return incompatibilities().contains(other.id()) ||
				other.incompatibilities().contains(this.id());
	}

	/**
	 * Checks if this modifier is incompatible with any in the given set.
	 *
	 * @param others The set of modifiers to check against
	 * @return true if any modifier in the set is incompatible
	 */
	default boolean isIncompatibleWithAny(Iterable<StatusModifier> others) {
		for (StatusModifier other : others) {
			if (isIncompatibleWith(other)) {
				return true;
			}
		}
		return false;
	}
}
