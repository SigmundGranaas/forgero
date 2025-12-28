package com.sigmundgranaas.forgero.testutils;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.ExtensibleEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StaticEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StructuredEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StructuredExtensibleEquipment;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.ArrayList;
import java.util.List;

public class ToolBuilder extends BaseComponentBuilder<ToolBuilder> {
	private final List<ComponentPart> structureSlots = new ArrayList<>();
	private final List<ComponentUpgradeSlot> upgradeSlots = new ArrayList<>();

	public ToolBuilder(OpenIdentifier id) {
		super(id);
	}

	public ToolBuilder withPart(Component part, String slotId, OpenIdentifier slotType) {
		var id = TestIdentifiers.id(slotId);
		this.structureSlots.add(new ComponentPart(id, slotType, "", SlotValidator.requireTag(slotType), part));
		return this;
	}

	public ToolBuilder withComponentUpgradeSlot(ComponentUpgradeSlot slot) {
		this.upgradeSlots.add(slot);
		return this;
	}


	public Component build() {
		boolean hasStructure = !structureSlots.isEmpty();
		boolean hasUpgrades = !upgradeSlots.isEmpty();
		var structure = ComponentStructure.of(structureSlots);
		var upgrades = ComponentUpgrades.of(upgradeSlots);

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
