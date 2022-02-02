package com.sigmundgranaas.forgero.core.toolpart.strategy.realistic;

import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticPrimaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHeadStrategy;

public class RealisticHeadStrategy extends RealisticToolPartStrategy implements ToolPartHeadStrategy {
    private final RealisticPrimaryMaterial realisticPrimaryMaterial;

    public RealisticHeadStrategy(RealisticPrimaryMaterial material) {
        super(material);
        realisticPrimaryMaterial = material;
    }

    @Override
    public int getDamage() {
        return realisticPrimaryMaterial.getSharpness() / 10;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return (float) (realisticPrimaryMaterial.getStiffness() / 20.0);
    }

    @Override
    public int getMiningLevel() {
        return (int) (realisticPrimaryMaterial.getStiffness() / 20.0);
    }

    @Override
    public ForgeroMaterial getMaterial() {
        return realisticPrimaryMaterial;
    }

    @Override
    public PrimaryMaterial getPrimaryMaterial() {
        return realisticPrimaryMaterial;
    }

    @Override
    public SecondaryMaterial getSecondaryMaterial() {
        return new EmptySecondaryMaterial();
    }
}
