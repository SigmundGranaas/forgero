package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The most common type of advanced equipment. It is composed of required parts
 * AND has optional slots for upgrades.
 * Example: A diamond pickaxe with head and handle slots, plus a binding upgrade slot.
 */
public record StructuredExtensibleEquipment(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		Map<String, List<?>> properties,
		ComponentStructure structure,
		ComponentUpgrades upgrades
) implements StructuredComponent, CustomizableComponent {

	public StructuredExtensibleEquipment {
		// Validate that slot IDs are unique across both structure and upgrades.
		var ids = new HashSet<>(structure.slots().ids());
		for (OpenIdentifier upgradeSlotId : upgrades.slots().ids()) {
			if (!ids.add(upgradeSlotId)) {
				throw new IllegalArgumentException("Duplicate slot ID found between structure and upgrades: " + upgradeSlotId);
			}
		}
	}

	@Override
	public Map<String, List<?>> propertiesAsMap() {
		return properties;
	}

	@Override
	public Set<OpenIdentifier> getTags() {
		return tags;
	}

	@Override
	public List<Component> getChildren() {
		return Stream.concat(
				structure.children().stream(),
				upgrades.filledContents().stream()
		).toList();
	}

	@Override
	public Component withStructure(ComponentStructure newStructure) {
		return new StructuredExtensibleEquipment(this.id, this.tags, this.properties, newStructure, this.upgrades);
	}

	@Override
	public Component withUpgrades(ComponentUpgrades newUpgrades) {
		return new StructuredExtensibleEquipment(this.id, this.tags, this.properties, this.structure, newUpgrades);
	}

	@Override
	public Component withProperties(Map<String, List<?>> newProperties) {
		return new StructuredExtensibleEquipment(this.id, this.tags, PropertyMergeHelper.merge(this.properties, newProperties), this.structure, this.upgrades);
	}
}
