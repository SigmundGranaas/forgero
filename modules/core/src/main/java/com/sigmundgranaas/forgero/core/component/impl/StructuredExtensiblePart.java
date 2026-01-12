package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ContributingComponent;
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
) implements StructuredComponent, CustomizableComponent, ContributingComponent {

	private static final OpenIdentifier TYPE_IDENTIFIER = OpenIdentifier.of("structured_extensible_part");

	@Override
	public OpenIdentifier getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}

	public StructuredExtensiblePart {
		// Validate that slot IDs are unique across both structure and upgrades.
		var ids = new HashSet<>(structure.parts().keySet());
		for (var upgradeSlot : upgrades.slots().all()) {
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
				upgrades.filledContents().stream()
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
		return new StructuredExtensiblePart(this.id, this.tags, PropertyMergeHelper.merge(this.properties, newProperties), this.structure, this.upgrades);
	}
}
