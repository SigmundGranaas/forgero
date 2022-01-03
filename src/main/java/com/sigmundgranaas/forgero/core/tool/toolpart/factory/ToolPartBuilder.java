package com.sigmundgranaas.forgero.core.tool.toolpart.factory;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;

public abstract class ToolPartBuilder {
    protected final PrimaryMaterial primary;
    protected SecondaryMaterial secondary;
    protected Gem gem;


    public ToolPartBuilder(PrimaryMaterial primary) {
        this.primary = primary;
        this.secondary = new EmptySecondaryMaterial();
        this.gem = null;
    }

    public ToolPartBuilder(ForgeroToolPart part) {
        this.primary = part.getPrimaryMaterial();
        this.secondary = part.getSecondaryMaterial();
        this.gem = null;
    }

    public PrimaryMaterial getPrimary() {
        return primary;
    }

    public SecondaryMaterial getSecondary() {
        return secondary;
    }

    public void setSecondary(SecondaryMaterial secondary) {
        this.secondary = secondary;
    }

    public Gem getGem() {
        return gem;
    }

    public void setGem(Gem gem) {
        this.gem = gem;
    }

    public abstract ForgeroToolPart createToolPart();
}
