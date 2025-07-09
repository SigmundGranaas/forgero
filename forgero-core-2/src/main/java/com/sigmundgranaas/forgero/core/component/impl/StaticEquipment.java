package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;
import java.util.List;
import java.util.Set;

/**
 * A piece of equipment that is a single, indivisible item.
 * It has no internal structure and cannot be customized.
 * Example: A simple flint knife or a magical artifact.
 */
public record StaticEquipment(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		List<Property> properties
) implements Component {

	@Override
	public List<Property> getProperties() {
		return properties;
	}

	@Override
	public Set<OpenIdentifier> getTags() {
		return tags;
	}
}
