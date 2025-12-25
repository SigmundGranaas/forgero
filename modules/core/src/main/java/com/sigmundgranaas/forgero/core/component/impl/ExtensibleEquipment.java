package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A piece of equipment that is a single item but has slots for upgrades.
 * Example: A magic amulet with a slot for a gem.
 */
public record ExtensibleEquipment(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		Map<String, List<?>> properties,
		ComponentUpgrades upgrades
) implements CustomizableComponent {

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
		return upgrades.slots().stream()
				.flatMap(slot -> slot.content().stream())
				.toList();
	}

	@Override
	public Component withUpgrades(ComponentUpgrades newUpgrades) {
		return new ExtensibleEquipment(this.id, this.tags, this.properties, newUpgrades);
	}

	@Override
	public Component withProperties(Map<String, List<?>> newProperties) {
		return new ExtensibleEquipment(this.id, this.tags, PropertyMergeHelper.merge(this.properties, newProperties), this.upgrades);
	}
}
