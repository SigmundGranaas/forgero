package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Equipment composed of a fixed set of parts, but with no optional upgrade slots.
 * Example: A basic wooden pickaxe that does not allow for a binding.
 */
public record StructuredEquipment(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		Map<String, List<?>> properties,
		ComponentStructure structure
) implements StructuredComponent {

	@Override
	public Map<String, List<?>> propertiesAsMap() {
		return properties;
	}

	@Override
	public Set<OpenIdentifier> getTags() {
		return tags;
	}

	@Override
	public Component withStructure(ComponentStructure newStructure) {
		return new StructuredEquipment(this.id, this.tags, this.properties, newStructure);
	}

	@Override
	public Component withProperties(Map<String, List<?>> newProperties) {
		Map<String, List<?>> merged = new HashMap<>(this.properties);
		newProperties.forEach((key, value) -> merged.merge(key, value, (existing, incoming) -> {
			List<Object> combined = new ArrayList<>(existing);
			combined.addAll(incoming);
			return combined;
		}));
		return new StructuredEquipment(this.id, this.tags, Collections.unmodifiableMap(merged), this.structure);
	}
}
