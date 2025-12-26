package com.sigmundgranaas.forgero.model.util;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.model.api.ModelSlot;
import com.sigmundgranaas.forgero.model.api.Slotted;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Modular utility for working with Slotted models using composition.
 *
 * This utility works with ANY model that implements the Slotted capability,
 * enabling slot-based operations without inheritance.
 */
public class SlotResolver {
	/**
	 * Matches model slots with filled component slots.
	 *
	 * @param slotted   The slotted model (can be ArmorModel, CompositeModel, or any Slotted)
	 * @param component The structured component containing filled slots
	 * @return A map of slot ID to component for all filled slots
	 */
	public static Map<String, Component> matchSlots(Slotted slotted, StructuredComponent component) {
		Map<String, Component> filledSlots = new HashMap<>();

		// Build map of filled component slots
		Map<String, Component> componentSlots = new HashMap<>();
		component.structure().slots().all().forEach(slot ->
				componentSlots.put(slot.id().path(), slot.content())
		);

		// Match model slots with component slots
		for (ModelSlot modelSlot : slotted.slots()) {
			Component slotContent = componentSlots.get(modelSlot.id());
			if (slotContent != null && !isEmptySlot(slotContent)) {
				filledSlots.put(modelSlot.id(), slotContent);
			}
		}

		return filledSlots;
	}

	/**
	 * Finds a component in a specific slot.
	 *
	 * @param slotted   The slotted model
	 * @param component The structured component
	 * @param slotId    The slot ID to look for
	 * @return An optional containing the component if the slot is filled
	 */
	public static Optional<Component> findInSlot(Slotted slotted, StructuredComponent component, String slotId) {
		// Check if the model has this slot
		boolean hasSlot = slotted.slots().stream()
				.anyMatch(slot -> slot.id().equals(slotId));

		if (!hasSlot) {
			return Optional.empty();
		}

		// Find the component in this slot
		return component.structure().slots().all().stream()
				.filter(slot -> slot.id().path().equals(slotId))
				.map(StructureSlot::content)
				.filter(SlotResolver::isNotEmptySlot)
				.findFirst();
	}

	/**
	 * Checks if a component represents an empty slot.
	 */
	private static boolean isEmptySlot(Component component) {
		return component == null || component.id().path().contains("empty");
	}

	/**
	 * Checks if a component is not an empty slot.
	 */
	private static boolean isNotEmptySlot(Component component) {
		return !isEmptySlot(component);
	}
}
