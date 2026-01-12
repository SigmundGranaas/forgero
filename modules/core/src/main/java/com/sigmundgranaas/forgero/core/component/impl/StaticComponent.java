package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ContributingComponent;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

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
) implements ContributingComponent {

	private static final OpenIdentifier TYPE_IDENTIFIER = OpenIdentifier.of("static_component");

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
	public Component withProperties(Map<String, List<?>> newProperties) {
		return new StaticComponent(this.id, this.tags, PropertyMergeHelper.merge(this.properties, newProperties));
	}
}
