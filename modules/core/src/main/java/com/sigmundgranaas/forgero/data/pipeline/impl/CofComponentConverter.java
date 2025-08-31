package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.cof.dto.CofSlot;
import com.sigmundgranaas.forgero.cof.dto.CofUpgrades;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.MaterialData;
import com.sigmundgranaas.forgero.data.loading.api.data.SchematicData;
import com.sigmundgranaas.forgero.data.loading.api.data.ShapeData;
import com.sigmundgranaas.forgero.data.loading.api.data.StaticData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;

import java.util.List;

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
		Object dto = rawDef.data();

		// Determine component type and upgrades based on the DTO
		OpenIdentifier componentType;
		CofUpgrades upgrades = null;

		if (dto instanceof StaticData staticDto) {
			if (staticDto.upgrades() != null && !staticDto.upgrades().isEmpty()) {
				componentType = idFactory.of("extensible_part");
				upgrades = convertUpgrades(staticDto.upgrades());
			} else {
				componentType = idFactory.of("static_component");
			}
		} else if (dto instanceof MaterialData || dto instanceof ShapeData || dto instanceof SchematicData) {
			componentType = idFactory.of("static_component");
		} else {
			// This should not happen if the initializer's logic is correct (skipping templates)
			throw new IllegalArgumentException("Unsupported DTO type for static conversion: " + dto.getClass().getName());
		}

		return new CofComponent(
				rawDef.id(),
				componentType,
				merged.tags(),
				merged.properties(),
				null, // Static components do not have a structure by this definition
				upgrades,
				1 // COF Version
		);
	}

	private CofUpgrades convertUpgrades(List<UpgradeSlotData> upgradeDataList) {
		if (upgradeDataList == null || upgradeDataList.isEmpty()) {
			return null;
		}
		var slots = upgradeDataList.stream()
				.map(upgrade -> new CofSlot(upgrade.id(), upgrade.type(), upgrade.description(), null, upgrade.tags()))
				.toList();
		return new CofUpgrades(slots);
	}
}
