package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;
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
}
