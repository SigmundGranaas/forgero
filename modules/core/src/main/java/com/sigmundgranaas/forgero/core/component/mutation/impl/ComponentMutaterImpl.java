package com.sigmundgranaas.forgero.core.component.mutation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the component mutation service.
 * <p>
 * This implementation delegates validation to the slots themselves,
 * ensuring consistent validation behavior.
 */
public class ComponentMutaterImpl implements ComponentMutater {

	@Override
	public Component apply(Component base, Mutation mutation) {
		Component current = base;

		// 1. Apply properties first
		if (!mutation.properties().isEmpty()) {
			current = current.withProperties(mutation.properties());
		}

		// 2. Apply structure changes
		for (Map.Entry<OpenIdentifier, Component> entry : mutation.structure().entrySet()) {
			current = setSlot(current, entry.getKey(), entry.getValue());
		}

		// 3. Apply upgrade changes
		for (Map.Entry<OpenIdentifier, Component> entry : mutation.upgrades().entrySet()) {
			current = setSlot(current, entry.getKey(), entry.getValue());
		}

		return current;
	}

	@Override
	public Component setSlot(Component target, OpenIdentifier slotId, Component newContent) {
		Slot targetSlot = findSlot(target, slotId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid slot ID: " + slotId));

		// Slots validate their own content, so we just call withContent
		if (targetSlot instanceof StructureSlot structureSlot) {
			StructureSlot updated = structureSlot.withContent(newContent);
			return setStructureSlot(target, updated);
		} else if (targetSlot instanceof UpgradeSlot upgradeSlot) {
			UpgradeSlot updated = upgradeSlot.withContent(newContent);
			return setUpgradeSlot(target, updated);
		}

		throw new IllegalStateException("Unknown slot type: " + targetSlot.getClass());
	}

	@Override
	public Component removeSlot(Component target, OpenIdentifier slotId) {
		Slot targetSlot = findSlot(target, slotId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid slot ID: " + slotId));

		if (targetSlot.isRequired()) {
			throw new IllegalArgumentException("Cannot remove content from a required slot (ID: " + slotId + ")");
		}

		if (targetSlot instanceof UpgradeSlot upgradeSlot) {
			return setUpgradeSlot(target, upgradeSlot.empty());
		}

		throw new IllegalStateException("Attempted to remove from a non-upgrade slot");
	}

	private Component setStructureSlot(Component target, StructureSlot updatedSlot) {
		if (!(target instanceof StructuredComponent structured)) {
			throw new IllegalArgumentException("Target component does not have a structure to modify.");
		}

		ComponentStructure newStructure = structured.structure().withSlot(updatedSlot);
		return structured.withStructure(newStructure);
	}

	private Component setUpgradeSlot(Component target, UpgradeSlot updatedSlot) {
		if (!(target instanceof CustomizableComponent customizable)) {
			throw new IllegalArgumentException("Target component is not customizable.");
		}

		ComponentUpgrades newUpgrades = customizable.upgrades().withSlot(updatedSlot);
		return customizable.withUpgrades(newUpgrades);
	}

	@Override
	public List<Slot> getAllSlots(Component component) {
		List<Slot> slots = new ArrayList<>();

		if (component instanceof StructuredComponent structured) {
			slots.addAll(structured.structure().slots().all());
		}
		if (component instanceof CustomizableComponent customizable) {
			slots.addAll(customizable.upgrades().slots().all());
		}

		return Collections.unmodifiableList(slots);
	}
}
