package com.sigmundgranaas.forgero.core.component.variant;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;
import java.util.List;
import java.util.Set;

/**
 * The simplest type of component. It is indivisible and has no upgrade slots.
 * This represents fundamental building blocks.
 * Example: An iron ingot, an oak log, or a simple gem.
 */
public record StaticComponent(
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
