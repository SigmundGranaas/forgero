package com.sigmundgranaas.forgerocore.toolpart.factory;

import com.sigmundgranaas.forgerocore.gem.EmptyGem;
import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;

public abstract class ToolPartBuilder {
    protected final PrimaryMaterial primary;
    protected SecondaryMaterial secondary;
    protected Gem gem;
    protected Schematic schematic;


    public ToolPartBuilder(PrimaryMaterial primary, Schematic pattern) {
        this.primary = primary;
        this.secondary = new EmptySecondaryMaterial();
        this.gem = EmptyGem.createEmptyGem();
        this.schematic = pattern;
    }

    public ToolPartBuilder(ForgeroToolPart part) {
        this.primary = part.getPrimaryMaterial();
        this.secondary = part.getSecondaryMaterial();
        this.gem = part.getGem();
        this.schematic = part.getSchematic();
    }

    public PrimaryMaterial getPrimary() {
        return primary;
    }

    public SecondaryMaterial getSecondary() {
        return secondary;
    }

    public ToolPartBuilder setSecondary(SecondaryMaterial secondary) {
        this.secondary = secondary;
        return this;
    }

    public Gem getGem() {
        return gem;
    }

    public abstract ToolPartBuilder setGem(Gem newGem);

    public Schematic getSchematic() {
        return schematic;
    }

    public abstract ForgeroToolPart createToolPart();
}
