package com.sigmundgranaas.forgero.core.material.material;

public interface SecondaryMaterial extends ForgeroMaterial {
    String getName();

    int getWeight();

    int getDurability();

    int getLuck();

    int getGrip();

    String getIngredientAsString();
}
