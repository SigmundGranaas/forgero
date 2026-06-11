package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.property.compiled.CompiledProperties;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.EquipmentComponent;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A piece of equipment that is a single, indivisible item.
 * It has no internal structure and cannot be customized.
 * Example: A simple flint knife or a magical artifact.
 */
public record StaticEquipment(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		Map<String, List<?>> properties,
		CompiledProperties compiled
) implements EquipmentComponent {

	private static final OpenIdentifier TYPE_IDENTIFIER = OpenIdentifier.of("static_equipment");

	/**
	 * Creates equipment with auto-baked attributes.
	 */
	public static StaticEquipment create(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			Map<String, List<?>> properties
	) {
		StaticEquipment temp = new StaticEquipment(id, tags, properties, CompiledProperties.EMPTY);
		return new StaticEquipment(id, tags, properties, ComponentCompiler.compile(temp));
	}

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
		Map<String, List<?>> merged = PropertyMergeHelper.merge(this.properties, newProperties);
		StaticEquipment temp = new StaticEquipment(this.id, this.tags, merged, CompiledProperties.EMPTY);
		return new StaticEquipment(this.id, this.tags, merged, ComponentCompiler.compile(temp));
	}
}
