package com.sigmundgranaas.forgero.core.toolpart.factory;

import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.pattern.Pattern;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;

public abstract class ToolPartBuilder {
    protected final PrimaryMaterial primary;
    protected SecondaryMaterial secondary;
    protected Gem gem;
    protected Pattern pattern;


    public ToolPartBuilder(PrimaryMaterial primary, Pattern pattern) {
        this.primary = primary;
        this.secondary = new EmptySecondaryMaterial();
        this.gem = EmptyGem.createEmptyGem();
        this.pattern = pattern;
    }

    public ToolPartBuilder(ForgeroToolPart part) {
        this.primary = part.getPrimaryMaterial();
        this.secondary = part.getSecondaryMaterial();
        this.gem = part.getGem();
        this.pattern = part.getPattern();
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

    public Pattern getPattern() {
        return pattern;
    }

    public abstract ForgeroToolPart createToolPart();
}
