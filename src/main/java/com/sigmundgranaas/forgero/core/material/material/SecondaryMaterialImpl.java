package com.sigmundgranaas.forgero.core.material.material;

public class SecondaryMaterialImpl extends AbstractMaterial implements SecondaryMaterial {
    public SecondaryMaterialImpl(MaterialPOJO material) {
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
