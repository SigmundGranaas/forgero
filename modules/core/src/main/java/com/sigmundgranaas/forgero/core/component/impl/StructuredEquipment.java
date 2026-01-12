package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.EquipmentComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

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
		ComponentStructure structure,
		BakedAttributes bakedAttributes
) implements StructuredComponent, EquipmentComponent {

	private static final OpenIdentifier TYPE_IDENTIFIER = OpenIdentifier.of("structured_equipment");

	/**
	 * Creates equipment with auto-baked attributes.
	 */
	public static StructuredEquipment create(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			Map<String, List<?>> properties,
			ComponentStructure structure
	) {
		StructuredEquipment temp = new StructuredEquipment(id, tags, properties, structure, BakedAttributes.EMPTY);
		return new StructuredEquipment(id, tags, properties, structure, AttributeBaker.bake(temp));
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
	public Component withStructure(ComponentStructure newStructure) {
		StructuredEquipment temp = new StructuredEquipment(this.id, this.tags, this.properties, newStructure, BakedAttributes.EMPTY);
		return new StructuredEquipment(this.id, this.tags, this.properties, newStructure, AttributeBaker.bake(temp));
	}

	@Override
	public Component withProperties(Map<String, List<?>> newProperties) {
		Map<String, List<?>> merged = PropertyMergeHelper.merge(this.properties, newProperties);
		StructuredEquipment temp = new StructuredEquipment(this.id, this.tags, merged, this.structure, BakedAttributes.EMPTY);
		return new StructuredEquipment(this.id, this.tags, merged, this.structure, AttributeBaker.bake(temp));
	}
}
