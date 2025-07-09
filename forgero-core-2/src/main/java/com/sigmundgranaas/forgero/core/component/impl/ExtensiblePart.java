package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;
import java.util.List;
import java.util.Set;

/**
 * An indivisible part that can be upgraded.
 * Example: A reinforced binding with a slot for a gem.
 */
public record ExtensiblePart(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		List<Property> properties,
		ComponentUpgrades upgrades
) implements CustomizableComponent {

	@Override
	public List<Property> getProperties() {
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
		return new ExtensiblePart(this.id, this.tags, this.properties, newUpgrades);
	}
}
