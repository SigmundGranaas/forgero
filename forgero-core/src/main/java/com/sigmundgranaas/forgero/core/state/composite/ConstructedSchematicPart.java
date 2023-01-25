package com.sigmundgranaas.forgero.core.state.composite;

import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.MaterialBased;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;

import java.util.List;

public class ConstructedSchematicPart extends ConstructedComposite implements MaterialBased {
    private final State schematic;
    private final State baseMaterial;

    public ConstructedSchematicPart(State schematic, State baseMaterial, SlotContainer slots, IdentifiableContainer id) {
        super(slots, id, List.of(schematic, baseMaterial));
        this.schematic = schematic;
        this.baseMaterial = baseMaterial;
    }

    @Override
    public State baseMaterial() {
        return baseMaterial;
    }

    @Override
    public ConstructedSchematicPart upgrade(State upgrade) {
        return null;
    }


    @Override
    public ConstructedSchematicPart removeUpgrade(String id) {
        return null;
    }

    public State getSchematic() {
        return schematic;
    }
}
