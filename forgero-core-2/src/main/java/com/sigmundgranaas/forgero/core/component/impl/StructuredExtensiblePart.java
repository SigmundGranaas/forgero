package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A composite part that also has its own upgrade slots.
 * Example: A runic pickaxe head, constructed from a schematic and metal,
 * which also has a dedicated slot for a power rune.
 */
public record StructuredExtensiblePart(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		Map<String, List<?>> properties,
		ComponentStructure structure,
		ComponentUpgrades upgrades
) implements StructuredComponent, CustomizableComponent {
	public StructuredExtensiblePart {
		// Validate that slot IDs are unique across both structure and upgrades.
		var ids = new HashSet<>(structure.slots().keySet()); // Get all IDs from the structure map's keys
		for (Slot upgradeSlot : upgrades.slots()) {
			if (!ids.add(upgradeSlot.id())) {
				throw new IllegalArgumentException("Duplicate slot ID found between structure and upgrades: " + upgradeSlot.id());
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
				upgrades.slots().stream().flatMap(slot -> slot.content().stream())
		).toList();
	}

	@Override
	public Component withStructure(ComponentStructure newStructure) {
		return new StructuredExtensiblePart(this.id, this.tags, this.properties, newStructure, this.upgrades);
	}

	@Override
	public Component withUpgrades(ComponentUpgrades newUpgrades) {
		return new StructuredExtensiblePart(this.id, this.tags, this.properties, this.structure, newUpgrades);
	}

	@Override
	public Component withProperties(Map<String, List<?>> newProperties) {
		Map<String, List<?>> merged = new HashMap<>(this.properties);
		newProperties.forEach((key, value) -> merged.merge(key, value, (existing, incoming) -> {
			List<Object> combined = new ArrayList<>(existing);
			combined.addAll(incoming);
			return combined;
		}));
		return new StructuredExtensiblePart(this.id, this.tags, Collections.unmodifiableMap(merged), this.structure, this.upgrades);
	}
}
