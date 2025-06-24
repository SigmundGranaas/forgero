package com.sigmundgranaas.forgero.core.component.mutation.api;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;

import java.util.List;
import java.util.Optional;

/**
 * A service for performing immutable state changes on Forgero components.
 * This service provides a high-level API for operations like swapping parts,
 * adding upgrades, or constructing tools, abstracting the underlying complexity
 * of reconstructing immutable component records.
 */
public interface ComponentMutater {
	/**
	 * Replaces the component in a specified slot or fills an empty slot.
	 * This operation is immutable; it returns a new component instance representing the new state.
	 *
	 * @param target     The component to modify.
	 * @param slotId     The unique identifier of the slot to update.
	 * @param newContent The new component to place in the slot.
	 * @return A new component instance with the updated slot.
	 * @throws IllegalArgumentException if the slotId is invalid, if the target has no slots,
	 *                                  or if the new content is not valid for the slot.
	 */
	Component setSlot(Component target, OpenIdentifier slotId, Component newContent);

	/**
	 * Removes the component from a specified optional (upgrade) slot.
	 * This operation is immutable and returns a new component instance.
	 *
	 * @param target The component to modify.
	 * @param slotId The unique identifier of the upgrade slot to empty.
	 * @return A new component instance with the slot emptied.
	 * @throws IllegalArgumentException if the slotId is invalid, if the slot is a required
	 *                                  structural slot, or if the target has no slots.
	 */
	Component removeSlot(Component target, OpenIdentifier slotId);

	/**
	 * A convenience method to find a specific slot by its identifier.
	 *
	 * @param component The component to search within.
	 * @param id        The identifier of the slot to find.
	 * @return An Optional containing the slot if found, otherwise empty.
	 */
	default Optional<Slot> findSlot(Component component, OpenIdentifier id) {
		return getAllSlots(component).stream()
				.filter(slot -> slot.id().equals(id))
				.findFirst();
	}

	/**
	 * Gathers all slots from a component, including both structural and upgrade slots.
	 *
	 * @param component The component to inspect.
	 * @return An unmodifiable list of all slots in the component.
	 */
	List<Slot> getAllSlots(Component component);
}
