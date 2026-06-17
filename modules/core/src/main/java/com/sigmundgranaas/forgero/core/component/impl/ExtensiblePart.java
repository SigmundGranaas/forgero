package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ContributingComponent;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An indivisible part that can be upgraded.
 * Example: A reinforced binding with a slot for a gem.
 */
public record ExtensiblePart(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		Map<String, List<?>> properties,
		ComponentUpgrades upgrades
) implements CustomizableComponent, ContributingComponent {

	private static final OpenIdentifier TYPE_IDENTIFIER = OpenIdentifier.of("extensible_part");

	@Override
	public OpenIdentifier getTypeIdentifier() {
		return TYPE_IDENTIFIER;
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
		return upgrades.traversableContents();
	}

	@Override
	public Component withUpgrades(ComponentUpgrades newUpgrades) {
		return new ExtensiblePart(this.id, this.tags, this.properties, newUpgrades);
	}

	@Override
	public Component withProperties(Map<String, List<?>> newProperties) {
		return new ExtensiblePart(this.id, this.tags, PropertyMergeHelper.merge(this.properties, newProperties), this.upgrades);
	}
}
