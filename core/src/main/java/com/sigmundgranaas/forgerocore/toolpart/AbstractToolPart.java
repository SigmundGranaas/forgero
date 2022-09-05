package com.sigmundgranaas.forgerocore.toolpart;

import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgerocore.property.Property;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.sigmundgranaas.forgerocore.identifier.Common.ELEMENT_SEPARATOR;

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
        return state.getSchematic();
    }

    @Override
    public ToolPartState getState() {
        return state;
    }

    @Override
    public String getToolPartIdentifier() {
        return getPrimaryMaterial().getResourceName() + ELEMENT_SEPARATOR + getSchematic().getResourceName();
    }

    @Override
    public PrimaryMaterial getPrimaryMaterial() {
        return state.getPrimaryMaterial();
    }

    @Override
    public SecondaryMaterial getSecondaryMaterial() {
        return state.getSecondaryMaterial();
    }

    @Override
    public @NotNull List<Property> getProperties() {
        return state.getProperties();
    }
}
