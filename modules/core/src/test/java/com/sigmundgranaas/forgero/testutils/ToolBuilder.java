package com.sigmundgranaas.forgero.testutils;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.impl.ExtensibleEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StaticEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StructuredEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StructuredExtensibleEquipment;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolBuilder extends BaseComponentBuilder<ToolBuilder> {
	private final Map<OpenIdentifier, StructureSlot> structureSlots = new HashMap<>();
	private final List<UpgradeSlot> upgradeSlots = new ArrayList<>();

	public ToolBuilder(OpenIdentifier id) {
		super(id);
	}

	public ToolBuilder withPart(Component part, String slotId, OpenIdentifier slotType) {
		var id = TestIdentifiers.id(slotId);
		this.structureSlots.put(id, new StructureSlot(id, slotType, "", part));
		return this;
	}

	public ToolBuilder withUpgradeSlot(UpgradeSlot slot) {
		this.upgradeSlots.add(slot);
		return this;
	}


	public Component build() {
		boolean hasStructure = !structureSlots.isEmpty();
		boolean hasUpgrades = !upgradeSlots.isEmpty();
		var structure = new ComponentStructure(structureSlots);
		var upgrades = new ComponentUpgrades(upgradeSlots);

		if (hasStructure && hasUpgrades) {
			return new StructuredExtensibleEquipment(id, tags, properties, structure, upgrades);
		}
		if (hasStructure) {
			return new StructuredEquipment(id, tags, properties, structure);
		}
		if (hasUpgrades) {
			return new ExtensibleEquipment(id, tags, properties, upgrades);
		}
		return new StaticEquipment(id, tags, properties);
	}
}
