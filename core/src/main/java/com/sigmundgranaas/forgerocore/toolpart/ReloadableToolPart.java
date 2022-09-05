package com.sigmundgranaas.forgerocore.toolpart;

import com.sigmundgranaas.forgerocore.ForgeroRegistry;
import com.sigmundgranaas.forgerocore.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgerocore.gem.EmptyGem;
import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgerocore.schematic.Schematic;

import static com.sigmundgranaas.forgerocore.identifier.Common.ELEMENT_SEPARATOR;

public abstract class ReloadableToolPart extends AbstractToolPart {

    public ReloadableToolPart(ToolPartState state) {
        super(state);
    }

    @Override
    public Gem getGem() {
        if (super.getGem() instanceof EmptyGem) {
            return super.getGem();
        }
        return ForgeroRegistry.GEM.getResource(super.getGem()).createGem((super.getGem()).getLevel());
    }

    @Override
    public Schematic getSchematic() {
        return ForgeroRegistry.SCHEMATIC.getResource(super.getSchematic());
    }

    @Override
    public ToolPartState getState() {
        return new ToolPartState(getPrimaryMaterial(), getSecondaryMaterial(), getGem(), getSchematic()) {
            @Override
            public ForgeroToolPartTypes getToolPartType() {
                return state.getToolPartType();
            }
        };
    }

    @Override
    public String getToolPartIdentifier() {
        return state.getPrimaryMaterial().getResourceName() + ELEMENT_SEPARATOR + getSchematic().getResourceName();
    }

    @Override
    public PrimaryMaterial getPrimaryMaterial() {
        return ForgeroRegistry.MATERIAL.getPrimaryMaterial(super.getPrimaryMaterial().getStringIdentifier()).orElse(super.getPrimaryMaterial());
    }

    @Override
    public SecondaryMaterial getSecondaryMaterial() {
        if (super.getSecondaryMaterial() instanceof EmptySecondaryMaterial) {
            return super.getSecondaryMaterial();
        }
        return ForgeroRegistry.MATERIAL.getSecondaryMaterial(super.getSecondaryMaterial().getStringIdentifier()).orElse(super.getSecondaryMaterial());
    }

    @Override
    public ToolPartPojo toDataResource() {
        return new ToolPartPojo();
    }
}
