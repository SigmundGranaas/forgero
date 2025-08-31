package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

	@Override
	public Component withProperties(Map<String, List<?>> newProperties) {
		Map<String, List<?>> merged = new HashMap<>(this.properties);
		newProperties.forEach((key, value) -> merged.merge(key, value, (existing, incoming) -> {
			List<Object> combined = new ArrayList<>(existing);
			combined.addAll(incoming);
			return combined;
		}));
		return new StaticComponent(this.id, this.tags, Collections.unmodifiableMap(merged));
	}
}
