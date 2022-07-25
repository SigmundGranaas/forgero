package com.sigmundgranaas.forgero.core.toolpart.state;

import com.sigmundgranaas.forgero.core.constructable.EmptyUpgradeSlot;
import com.sigmundgranaas.forgero.core.constructable.Upgrade;
import com.sigmundgranaas.forgero.core.constructable.UpgradeSlot;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.trinket.Trinket;

import java.util.List;

public class SchematicToolPartStateBuilder<T extends ForgeroToolPart> implements StateBuilder<T> {
    private final PrimaryMaterial primary;
    private final Schematic schematic;
    private final List<EmptyUpgradeSlot<Upgrade>> upgradeSlots;

    public SchematicToolPartStateBuilder(PrimaryMaterial primaryMaterial, Schematic schematic) {
    this.primary = primaryMaterial;
    this.schematic = schematic;
    this.upgradeSlots = schematic.createUpgradeSlots();
    }

    @Override
    public ToolPartState<T> build() {
        return null;
    }

    @Override
    public SchematicToolPartStateBuilder<T> set(int index, Upgrade upgrade) {
        return this;
    }

    @Override
    public SchematicToolPartStateBuilder<T> add(Upgrade upgrade) {
        return this;
    }
}
