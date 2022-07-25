package com.sigmundgranaas.forgero.core.toolpart.state;

import com.sigmundgranaas.forgero.core.constructable.Construct;
import com.sigmundgranaas.forgero.core.constructable.PrimaryMaterialConstructable;
import com.sigmundgranaas.forgero.core.constructable.SchematicConstructable;
import com.sigmundgranaas.forgero.core.constructable.Slot;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.List;

public record DefaultToolPartState<T extends ForgeroToolPart>(Construct<Schematic> schematicConstruct, Construct<PrimaryMaterial> materialConstruct, List<Slot<?>> slots, ForgeroToolPartTypes type) implements ToolPartState<T>, PrimaryMaterialConstructable, SchematicConstructable {

    @Override
    public List<Construct<?>> getConstructs() {
        return List.of(getPrimaryMaterial(), getSchematic());
    }

    @Override
    public int getTotalUpgradeSlots() {
        return slots.size();
    }

    @Override
    public ForgeroToolPartTypes getType() {
        return type;
    }

    @Override
    public PrimaryMaterial getPrimaryMaterial() {
        return materialConstruct.getResource();
    }

    @Override
    public Schematic getSchematic() {
        return schematicConstruct.getResource();
    }
}
