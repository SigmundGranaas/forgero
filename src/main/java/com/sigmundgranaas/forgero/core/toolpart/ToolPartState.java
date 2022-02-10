package com.sigmundgranaas.forgero.core.toolpart;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;

public abstract class ToolPartState {
    final PrimaryMaterial primaryMaterial;
    final SecondaryMaterial secondaryMaterial;
    final Gem gem;

    public ToolPartState(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial, Gem gem) {
        this.primaryMaterial = primaryMaterial;
        this.secondaryMaterial = secondaryMaterial;
        this.gem = gem;
    }

    public PrimaryMaterial getPrimaryMaterial() {
        return primaryMaterial;
    }

    public SecondaryMaterial getSecondaryMaterial() {
        return secondaryMaterial;
    }

    public Gem getGem() {
        return gem;
    }
}
