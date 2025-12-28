package com.sigmundgranaas.forgero.testutils;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.*;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.ArrayList;
import java.util.List;

public class PartBuilder extends BaseComponentBuilder<PartBuilder> {
	private final List<ComponentPart> structureSlots = new ArrayList<>();
	private final List<ComponentUpgradeSlot> upgradeSlots = new ArrayList<>();

	public PartBuilder(OpenIdentifier id) {
		super(id);
	}

	public PartBuilder withStructureSlot(ComponentPart slot) {
		this.structureSlots.add(slot);
		return this;
	}

	public PartBuilder withComponentUpgradeSlot(ComponentUpgradeSlot slot) {
		this.upgradeSlots.add(slot);
		return this;
	}

	public Component build() {
		boolean hasStructure = !structureSlots.isEmpty();
		boolean hasUpgrades = !upgradeSlots.isEmpty();
		var structure = ComponentStructure.of(structureSlots);
		var upgrades = ComponentUpgrades.of(upgradeSlots);

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
