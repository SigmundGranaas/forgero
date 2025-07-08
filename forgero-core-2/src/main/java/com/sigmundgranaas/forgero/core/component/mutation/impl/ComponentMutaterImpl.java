package com.sigmundgranaas.forgero.core.component.mutation.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ComponentMutaterImpl implements ComponentMutater {

	@Override
	public Component setSlot(Component target, OpenIdentifier slotId, Component newContent) {
		Slot targetSlot = findSlot(target, slotId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid slot ID: " + slotId));

		if (targetSlot instanceof StructureSlot) {
			StructureSlot currentSlot = (StructureSlot) targetSlot;
			// Check if the new component has a tag that matches the slot's type
			if (!newContent.getTags().contains(currentSlot.type())) {
				throw new IllegalArgumentException(String.format("Component %s is not of required type %s for slot %s", newContent.id(), currentSlot.type(), slotId));
			}
			return setStructureSlot(target, slotId, newContent);
		} else if (targetSlot instanceof UpgradeSlot) {
			UpgradeSlot oldSlot = (UpgradeSlot) targetSlot;
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

		if (targetSlot instanceof UpgradeSlot) {
			UpgradeSlot oldSlot = (UpgradeSlot) targetSlot;
			return setUpgradeSlot(target, slotId, oldSlot.empty());
		}
		throw new IllegalStateException("Attempted to remove from a non-upgrade slot, this should not be possible.");
	}

	private Component setStructureSlot(Component target, OpenIdentifier slotId, Component newContent) {
		if (!(target instanceof StructuredComponent)) {
			throw new IllegalArgumentException("Target component does not have a structure to modify.");
		}
		StructuredComponent structured = (StructuredComponent) target;
		List<StructureSlot> oldSlots = structured.structure().slots();
		List<StructureSlot> newSlots = oldSlots.stream()
				.map(slot -> {
					if (slot.id().equals(slotId)) {
						return new StructureSlot(slot.id(), slot.type(), slot.description(), newContent);
					}
					return slot;
				})
				.collect(Collectors.toList());
		ComponentStructure newStructure = new ComponentStructure(newSlots);

		return structured.withStructure(newStructure);
	}

	private Component setUpgradeSlot(Component target, OpenIdentifier slotId, UpgradeSlot newSlot) {
		if (!(target instanceof CustomizableComponent)) {
			throw new IllegalArgumentException("Target component is not customizable.");
		}
		CustomizableComponent customizable = (CustomizableComponent) target;
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
		if (component instanceof StructuredComponent) {
			slots.addAll(((StructuredComponent) component).structure().slots());
		}
		if (component instanceof CustomizableComponent) {
			slots.addAll(((CustomizableComponent) component).upgrades().slots());
		}
		return Collections.unmodifiableList(slots);
	}
}
