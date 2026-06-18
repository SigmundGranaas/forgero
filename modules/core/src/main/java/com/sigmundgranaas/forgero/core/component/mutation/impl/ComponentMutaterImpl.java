package com.sigmundgranaas.forgero.core.component.mutation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the component mutation service.
 * <p>
 * This implementation handles both immutable structure parts and mutable slots.
 * Structure parts validate content but are not part of the Slot system.
 */
public class ComponentMutaterImpl implements ComponentMutater {

	@Override
	public Component apply(Component base, Mutation mutation) {
		Component current = base;

		// 1. Apply properties first
		if (!mutation.properties().isEmpty()) {
			current = current.withProperties(mutation.properties());
		}

		// 2. Apply structure changes (ComponentPart, not Slots)
		for (Map.Entry<OpenIdentifier, Component> entry : mutation.structure().entrySet()) {
			current = setSlot(current, entry.getKey(), entry.getValue());
		}

		// 3. Apply upgrade changes (mutable Slots)
		for (Map.Entry<OpenIdentifier, Component> entry : mutation.upgrades().entrySet()) {
			current = setSlot(current, entry.getKey(), entry.getValue());
		}

		return current;
	}

	@Override
	public Component setSlot(Component target, OpenIdentifier slotId, Component newContent) {
		// Check structure parts first (not Slots)
		if (target instanceof StructuredComponent structured) {
			var part = structured.structure().getPart(slotId);
			if (part.isPresent()) {
				ComponentPart updated = part.get().withContent(newContent);
				return setStructurePart(target, updated);
			}
		}

		// Check direct mutable slots on this component (any slot kind)
		if (target instanceof CustomizableComponent customizable) {
			var directSlot = customizable.upgrades().getSlot(slotId);
			if (directSlot.isPresent()) {
				Slot updated = directSlot.get().withComponentContent(java.util.Optional.of(newContent));
				return setSlotInContainer(target, updated);
			}
		}

		// Check nested slots in structure parts (recursive)
		if (target instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				if (hasSlot(part.content(), slotId)) {
					// Recursively set the slot in the nested part
					Component updatedContent = setSlot(part.content(), slotId, newContent);
					ComponentPart updatedPart = part.withContent(updatedContent);
					return setStructurePart(target, updatedPart);
				}
			}
		}

		throw new IllegalArgumentException("Invalid slot or part ID: " + slotId);
	}

	/**
	 * Checks if a component (or its nested parts) contains a slot with the given ID.
	 */
	private boolean hasSlot(Component component, OpenIdentifier slotId) {
		// Check direct slots (any kind)
		if (component instanceof CustomizableComponent customizable) {
			if (customizable.upgrades().getSlot(slotId).isPresent()) {
				return true;
			}
		}
		// Check nested parts
		if (component instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				if (hasSlot(part.content(), slotId)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Component removeSlot(Component target, OpenIdentifier slotId) {
		// Structure parts cannot be removed
		if (target instanceof StructuredComponent structured) {
			if (structured.structure().contains(slotId)) {
				throw new IllegalArgumentException("Cannot remove structure part (ID: " + slotId + ")");
			}
		}

		// Check direct mutable slots on this component (any slot kind)
		if (target instanceof CustomizableComponent customizable) {
			var directSlot = customizable.upgrades().getSlot(slotId);
			if (directSlot.isPresent()) {
				Slot emptied = directSlot.get().withComponentContent(java.util.Optional.empty());
				return setSlotInContainer(target, emptied);
			}
		}

		// Check nested slots in structure parts (recursive)
		if (target instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				if (hasSlot(part.content(), slotId)) {
					// Recursively remove the slot content in the nested part
					Component updatedContent = removeSlot(part.content(), slotId);
					ComponentPart updatedPart = part.withContent(updatedContent);
					return setStructurePart(target, updatedPart);
				}
			}
		}

		throw new IllegalArgumentException("Invalid slot ID: " + slotId);
	}

	private Component setStructurePart(Component target, ComponentPart updatedPart) {
		if (!(target instanceof StructuredComponent structured)) {
			throw new IllegalArgumentException("Target component does not have a structure to modify.");
		}

		ComponentStructure newStructure = structured.structure().withPart(updatedPart);
		return structured.withStructure(newStructure);
	}

	private Component setSlotInContainer(Component target, Slot updatedSlot) {
		if (!(target instanceof CustomizableComponent customizable)) {
			throw new IllegalArgumentException("Target component is not customizable.");
		}

		ComponentUpgrades newUpgrades = customizable.upgrades().withAnySlot(updatedSlot);
		return customizable.withUpgrades(newUpgrades);
	}

	@Override
	public List<Slot> getAllSlots(Component component) {
		List<Slot> slots = new ArrayList<>();
		collectAllSlotsRecursive(component, slots);
		return Collections.unmodifiableList(slots);
	}

	/**
	 * Recursively collects all slots from a component and its nested structure parts.
	 */
	private void collectAllSlotsRecursive(Component component, List<Slot> slots) {
		// Collect direct slots
		if (component instanceof CustomizableComponent customizable) {
			slots.addAll(customizable.upgrades().slots().all());
		}

		// Recurse into structure parts
		if (component instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				collectAllSlotsRecursive(part.content(), slots);
			}
		}
	}
}
