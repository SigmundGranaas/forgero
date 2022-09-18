package com.sigmundgranaas.forgero.toolpart;

import com.sigmundgranaas.forgero.ForgeroRegistry;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgero.gem.EmptyGem;
import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.schematic.Schematic;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;

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
