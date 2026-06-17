package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.core.property.compiled.CompiledProperties;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.EquipmentComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A piece of equipment that is a single item but has slots for upgrades.
 * Example: A magic amulet with a slot for a gem.
 */
public record ExtensibleEquipment(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		Map<String, List<?>> properties,
		ComponentUpgrades upgrades,
		CompiledProperties compiled
) implements CustomizableComponent, EquipmentComponent {

	private static final OpenIdentifier TYPE_IDENTIFIER = OpenIdentifier.of("extensible_equipment");

	/**
	 * Creates equipment with auto-baked attributes.
	 */
	public static ExtensibleEquipment create(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			Map<String, List<?>> properties,
			ComponentUpgrades upgrades
	) {
		ExtensibleEquipment temp = new ExtensibleEquipment(id, tags, properties, upgrades, CompiledProperties.EMPTY);
		return new ExtensibleEquipment(id, tags, properties, upgrades, ComponentCompiler.compile(temp));
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
	public List<Component> getChildren() {
		return upgrades.traversableContents();
	}

	@Override
	public Component withUpgrades(ComponentUpgrades newUpgrades) {
		ExtensibleEquipment temp = new ExtensibleEquipment(this.id, this.tags, this.properties, newUpgrades, CompiledProperties.EMPTY);
		return new ExtensibleEquipment(this.id, this.tags, this.properties, newUpgrades, ComponentCompiler.compile(temp));
	}

	@Override
	public Component withProperties(Map<String, List<?>> newProperties) {
		Map<String, List<?>> merged = PropertyMergeHelper.merge(this.properties, newProperties);
		ExtensibleEquipment temp = new ExtensibleEquipment(this.id, this.tags, merged, this.upgrades, CompiledProperties.EMPTY);
		return new ExtensibleEquipment(this.id, this.tags, merged, this.upgrades, ComponentCompiler.compile(temp));
	}
}
