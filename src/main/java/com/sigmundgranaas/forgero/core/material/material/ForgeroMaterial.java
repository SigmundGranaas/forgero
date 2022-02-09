package com.sigmundgranaas.forgero.core.material.material;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroMaterial {
    int getRarity();

    @NotNull String getName();

    int getDurability();

    MaterialType getType();

    @NotNull List<String> getProperties();

    String getIngredient();
}
