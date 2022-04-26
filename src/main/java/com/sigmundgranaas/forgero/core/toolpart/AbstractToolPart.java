package com.sigmundgranaas.forgero.core.toolpart;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.schematic.Schematic;

public abstract class AbstractToolPart implements ForgeroToolPart {
    protected final ToolPartState state;


    public AbstractToolPart(ToolPartState state) {
        this.state = state;
    }

    @Override
    public Gem getGem() {
        return this.state.getGem();
    }

    @Override
    public Schematic getSchematic() {
        return this.state.getSchematic();
    }

    @Override
    public ToolPartState getState() {
        return state;
    }

    @Override
    public String getToolPartIdentifier() {
        return state.getPrimaryMaterial().getName() + "_" + getToolPartName() + "_" + getSchematic().getVariant();
    }

    @Override
    public PrimaryMaterial getPrimaryMaterial() {
        return state.getPrimaryMaterial();
    }

    @Override
    public SecondaryMaterial getSecondaryMaterial() {
        return state.getSecondaryMaterial();
    }
}
