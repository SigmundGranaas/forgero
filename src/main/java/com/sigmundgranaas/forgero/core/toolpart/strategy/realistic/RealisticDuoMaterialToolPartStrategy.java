package com.sigmundgranaas.forgero.core.toolpart.strategy.realistic;

import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticPrimaryMaterial;

public class RealisticDuoMaterialToolPartStrategy extends RealisticToolPartStrategy {
    protected final SecondaryMaterial secondaryMaterial;

    public RealisticDuoMaterialToolPartStrategy(RealisticPrimaryMaterial material, SecondaryMaterial secondaryMaterial) {
        super(material);
        this.secondaryMaterial = secondaryMaterial;
    }

    @Override
    public SecondaryMaterial getSecondaryMaterial() {
        return secondaryMaterial;
    }
}
