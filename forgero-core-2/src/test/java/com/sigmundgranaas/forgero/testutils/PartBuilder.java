package com.sigmundgranaas.forgero.testutils;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.impl.*;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartBuilder extends BaseComponentBuilder<PartBuilder> {
	private final Map<OpenIdentifier, StructureSlot> structureSlots = new HashMap<>();
	private final List<UpgradeSlot> upgradeSlots = new ArrayList<>();

	public PartBuilder(OpenIdentifier id) {
		super(id);
	}

	public PartBuilder withStructureSlot(StructureSlot slot) {
		this.structureSlots.put(slot.id(), slot);
		return this;
	}

	public PartBuilder withUpgradeSlot(UpgradeSlot slot) {
		this.upgradeSlots.add(slot);
		return this;
	}

	public Component build() {
		boolean hasStructure = !structureSlots.isEmpty();
		boolean hasUpgrades = !upgradeSlots.isEmpty();
		var structure = new ComponentStructure(structureSlots);
		var upgrades = new ComponentUpgrades(upgradeSlots);

		if (hasStructure && hasUpgrades) {
			return new StructuredExtensiblePart(id, tags, properties, structure, upgrades);
		}
		if (hasStructure) {
			return new StructuredPart(id, tags, properties, structure);
		}
		if (hasUpgrades) {
			return new ExtensiblePart(id, tags, properties, upgrades);
		}
		return new StaticComponent(id, tags, properties);
	}
}
