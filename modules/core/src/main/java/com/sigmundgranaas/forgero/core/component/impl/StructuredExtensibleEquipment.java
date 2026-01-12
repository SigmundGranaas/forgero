package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.EquipmentComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The most common type of advanced equipment. It is composed of required parts
 * AND has optional slots for upgrades.
 * Example: A diamond pickaxe with head and handle slots, plus a binding upgrade slot.
 */
public record StructuredExtensibleEquipment(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		Map<String, List<?>> properties,
		ComponentStructure structure,
		ComponentUpgrades upgrades,
		BakedAttributes bakedAttributes
) implements StructuredComponent, CustomizableComponent, EquipmentComponent {

	private static final OpenIdentifier TYPE_IDENTIFIER = OpenIdentifier.of("structured_extensible_equipment");

	/**
	 * Creates equipment with auto-baked attributes.
	 */
	public static StructuredExtensibleEquipment create(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			Map<String, List<?>> properties,
			ComponentStructure structure,
			ComponentUpgrades upgrades
	) {
		validateSlotIds(structure, upgrades);
		StructuredExtensibleEquipment temp = new StructuredExtensibleEquipment(
				id, tags, properties, structure, upgrades, BakedAttributes.EMPTY
		);
		return new StructuredExtensibleEquipment(
				id, tags, properties, structure, upgrades, AttributeBaker.bake(temp)
		);
	}

	public StructuredExtensibleEquipment {
		validateSlotIds(structure, upgrades);
	}

	private static void validateSlotIds(ComponentStructure structure, ComponentUpgrades upgrades) {
		var ids = new HashSet<>(structure.parts().keySet());
		for (var upgradeSlot : upgrades.slots().all()) {
			if (!ids.add(upgradeSlot.id())) {
				throw new IllegalArgumentException(
						"Duplicate slot ID found between structure and upgrades: " + upgradeSlot.id()
				);
			}
		}
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
		return Stream.concat(
				structure.children().stream(),
				upgrades.filledContents().stream()
		).toList();
	}

	@Override
	public Component withStructure(ComponentStructure newStructure) {
		StructuredExtensibleEquipment temp = new StructuredExtensibleEquipment(
				this.id, this.tags, this.properties, newStructure, this.upgrades, BakedAttributes.EMPTY
		);
		return new StructuredExtensibleEquipment(
				this.id, this.tags, this.properties, newStructure, this.upgrades, AttributeBaker.bake(temp)
		);
	}

	@Override
	public Component withUpgrades(ComponentUpgrades newUpgrades) {
		StructuredExtensibleEquipment temp = new StructuredExtensibleEquipment(
				this.id, this.tags, this.properties, this.structure, newUpgrades, BakedAttributes.EMPTY
		);
		return new StructuredExtensibleEquipment(
				this.id, this.tags, this.properties, this.structure, newUpgrades, AttributeBaker.bake(temp)
		);
	}

	@Override
	public Component withProperties(Map<String, List<?>> newProperties) {
		Map<String, List<?>> merged = PropertyMergeHelper.merge(this.properties, newProperties);
		StructuredExtensibleEquipment temp = new StructuredExtensibleEquipment(
				this.id, this.tags, merged, this.structure, this.upgrades, BakedAttributes.EMPTY
		);
		return new StructuredExtensibleEquipment(
				this.id, this.tags, merged, this.structure, this.upgrades, AttributeBaker.bake(temp)
		);
	}
}
