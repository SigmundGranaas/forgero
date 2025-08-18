package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The simplest type of component. It is indivisible and has no upgrade slots.
 * This represents fundamental building blocks.
 * Example: An iron ingot, an oak log, or a simple gem.
 */
public record StaticComponent(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		Map<String, List<?>> properties
) implements Component {

	@Override
	public Map<String, List<?>> propertiesAsMap() {
		return properties;
	}

	@Override
	public Set<OpenIdentifier> getTags() {
		return tags;
	}
}
