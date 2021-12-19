package com.sigmundgranaas.forgero.material.material;

public class EmptySecondaryMaterial implements SecondaryMaterial {
    @Override
    public String getName() {
        return "null";
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
