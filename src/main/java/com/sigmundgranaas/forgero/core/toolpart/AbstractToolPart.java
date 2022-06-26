package com.sigmundgranaas.forgero.core.toolpart;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.toolpart.state.AbstractToolPartState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

public abstract class AbstractToolPart implements ForgeroToolPart {
    protected final AbstractToolPartState state;


    public AbstractToolPart(AbstractToolPartState state) {
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
    public AbstractToolPartState getState() {
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
