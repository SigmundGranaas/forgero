package com.sigmundgranaas.forgero.core.component.mutation.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComponentMutaterImpl implements ComponentMutater {

	@Override
	public Component apply(Component base, Mutation mutation) {
		Component current = base;

		// 1. Apply properties first, letting the component reconstruct itself.
		if (!mutation.properties().isEmpty()) {
			current = current.withProperties(mutation.properties());
		}

		// 2. Apply structure changes using the existing low-level API.
		for (Map.Entry<OpenIdentifier, Component> entry : mutation.structure().entrySet()) {
			current = setSlot(current, entry.getKey(), entry.getValue());
		}

		// 3. Apply upgrade changes.
		for (Map.Entry<OpenIdentifier, Component> entry : mutation.upgrades().entrySet()) {
			current = setSlot(current, entry.getKey(), entry.getValue());
		}

		return current;
	}

	@Override
	public Component setSlot(Component target, OpenIdentifier slotId, Component newContent) {
		Slot targetSlot = findSlot(target, slotId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid slot ID: " + slotId));

		if (targetSlot instanceof StructureSlot currentSlot) {
			// Check if the new component has a tag that matches the slot's type
			if (!newContent.getTags().contains(currentSlot.type())) {
				throw new IllegalArgumentException(String.format("Component %s is not of required type %s for slot %s", newContent.id(), currentSlot.type(), slotId));
			}
			return setStructureSlot(target, slotId, newContent);
		} else if (targetSlot instanceof UpgradeSlot oldSlot) {
			UpgradeSlot newSlot = oldSlot.apply(newContent);
			return setUpgradeSlot(target, slotId, newSlot);
		}
		throw new IllegalStateException("Unknown slot type for component: " + target.id());
	}

	@Override
	public Component removeSlot(Component target, OpenIdentifier slotId) {
		Slot targetSlot = findSlot(target, slotId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid slot ID: " + slotId));

		if (targetSlot.isRequired()) {
			throw new IllegalArgumentException("Cannot remove content from a required slot (ID: " + slotId + ")");
		}

		if (targetSlot instanceof UpgradeSlot oldSlot) {
			return setUpgradeSlot(target, slotId, oldSlot.empty());
		}
		throw new IllegalStateException("Attempted to remove from a non-upgrade slot, this should not be possible.");
	}

	private Component setStructureSlot(Component target, OpenIdentifier slotId, Component newContent) {
		if (!(target instanceof StructuredComponent structured)) {
			throw new IllegalArgumentException("Target component does not have a structure to modify.");
		}
		// Get the current structure slots map
		Map<OpenIdentifier, StructureSlot> oldSlotsMap = structured.structure().slots();

		// Create a new mutable map to update the specific slot
		Map<OpenIdentifier, StructureSlot> newSlotsMap = new HashMap<>(oldSlotsMap);

		// Create the new StructureSlot with the updated content
		StructureSlot oldStructureSlot = oldSlotsMap.get(slotId);
		if (oldStructureSlot == null) {
			// This case should ideally be caught by findSlot, but as a safeguard.
			throw new IllegalArgumentException("Slot not found in structure: " + slotId);
		}
		StructureSlot updatedSlot = new StructureSlot(oldStructureSlot.id(), oldStructureSlot.type(), oldStructureSlot.description(), newContent);
		newSlotsMap.put(slotId, updatedSlot); // Replace the old slot with the updated one

		// Create a new ComponentStructure with the updated map
		ComponentStructure newStructure = new ComponentStructure(Collections.unmodifiableMap(newSlotsMap)); // Ensure immutability

		return structured.withStructure(newStructure);
	}

	private Component setUpgradeSlot(Component target, OpenIdentifier slotId, UpgradeSlot newSlot) {
		if (!(target instanceof CustomizableComponent customizable)) {
			throw new IllegalArgumentException("Target component is not customizable.");
		}
		List<UpgradeSlot> oldSlots = customizable.upgrades().slots();
		List<UpgradeSlot> newSlots = oldSlots.stream()
				.map(slot -> slot.id().equals(slotId) ? newSlot : slot)
				.collect(Collectors.toList());
		ComponentUpgrades newUpgrades = new ComponentUpgrades(newSlots);

		return customizable.withUpgrades(newUpgrades);
	}

	@Override
	public List<Slot> getAllSlots(Component component) {
		List<Slot> slots = new ArrayList<>();
		if (component instanceof StructuredComponent structured) {
			slots.addAll(structured.structure().slots().values());
		}
		if (component instanceof CustomizableComponent customizable) {
			slots.addAll(customizable.upgrades().slots());
		}
		return Collections.unmodifiableList(slots);
	}
}
