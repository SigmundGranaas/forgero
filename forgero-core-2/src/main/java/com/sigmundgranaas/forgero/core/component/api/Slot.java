package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;

import java.util.Optional;

/**
 * Common interface for slots in a component's structure.
 * A slot is an identified container within a component that can hold another component.
 */
public interface Slot {
	/**
	 * @return The unique identifier for this slot within the component.
	 */
	OpenIdentifier id();

	/**
	 * @return The type of component that can fit in this slot.
	 */
	OpenIdentifier type();

	/**
	 * @return A human-readable description of the slot.
	 */
	String description();

	/**
	 * @return The component currently held in this slot, if any.
	 */
	Optional<Component> get();

	/**
	 * @return true if this slot is a required part of the component's structure.
	 */
	boolean isRequired();
}
