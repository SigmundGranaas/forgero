package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;

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
