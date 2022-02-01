package com.sigmundgranaas.forgero.core.material.material.realistic;

import com.sigmundgranaas.forgero.core.material.material.AbstractForgeroMaterial;

public class SecondaryMaterialImpl extends AbstractForgeroMaterial implements RealisticSecondaryMaterial {
    public SecondaryMaterialImpl(RealisticMaterialPOJO material) {
        super(material);
    }

    @Override
    public int getLuck() {
        return 0;
    }

    @Override
    public int getGrip() {
        return 0;
    }

}
