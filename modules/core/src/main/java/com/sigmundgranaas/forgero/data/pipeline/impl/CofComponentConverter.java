package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.sigmundgranaas.forgero.cof.ComponentTypeRegistry;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.cof.dto.CofSlot;
import com.sigmundgranaas.forgero.cof.dto.CofUpgrades;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;

import java.util.List;
import java.util.Optional;

/**
 * Converts a raw definition and its merged properties into a basic CofComponent.
 * This converter handles non-template definitions like materials, shapes, and static parts.
 */
public class CofComponentConverter {

	private final IdentifierFactory idFactory;

	public CofComponentConverter(IdentifierFactory idFactory) {
		this.idFactory = idFactory;
	}

	public CofComponent convert(RawDefinition rawDef, PropertyMerger.MergedResult merged) {
		DefinitionData dto = rawDef.data();

		// Determine component type and upgrades based on the DTO
		OpenIdentifier componentType;
		CofUpgrades upgrades = null;

		if (dto instanceof ResourceData resourceData) {
			// ResourceData is the unified type for materials, shapes, schematics, casts, and static parts
			if (resourceData.upgrades() != null && !resourceData.upgrades().isEmpty()) {
				componentType = ComponentTypeRegistry.EXTENSIBLE_PART;
				upgrades = convertUpgrades(resourceData.upgrades());
			} else {
				componentType = ComponentTypeRegistry.STATIC_COMPONENT;
			}
		} else {
			// This should not happen if the initializer's logic is correct (skipping templates)
			throw new IllegalArgumentException("Unsupported DTO type for static conversion: " + dto.getClass().getName());
		}

		return new CofComponent(
				rawDef.id(),
				componentType,
				Optional.of(merged.tags()),
				Optional.of(merged.properties()),
				Optional.empty(), // Static components do not have a structure by this definition
				Optional.ofNullable(upgrades),
				Optional.of(1) // COF Version
		);
	}

	private CofUpgrades convertUpgrades(List<UpgradeSlotData> upgradeDataList) {
		if (upgradeDataList == null || upgradeDataList.isEmpty()) {
			return null;
		}
		var slots = upgradeDataList.stream()
				.map(upgrade -> new CofSlot(upgrade.id(), upgrade.type(), upgrade.description(), upgrade.context(), null, upgrade.tags()))
				.toList();
		return new CofUpgrades(slots);
	}
}
