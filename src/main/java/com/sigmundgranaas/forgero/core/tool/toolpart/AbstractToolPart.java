package com.sigmundgranaas.forgero.core.tool.toolpart;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.EmptySecondaryMaterial;

public abstract class AbstractToolPart implements ForgeroToolPart {
    private final PrimaryMaterial primaryMaterial;
    private final SecondaryMaterial secondaryMaterial;

    public AbstractToolPart(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial) {
        this.primaryMaterial = primaryMaterial;
        this.secondaryMaterial = secondaryMaterial;
    }

    public AbstractToolPart(PrimaryMaterial primaryMaterial) {
        this.primaryMaterial = primaryMaterial;
        this.secondaryMaterial = new EmptySecondaryMaterial();
    }


    @Override
    public PrimaryMaterial getPrimaryMaterial() {
        return primaryMaterial;
    }

    @Override
    public SecondaryMaterial getSecondaryMaterial() {
        return secondaryMaterial;
    }

}
