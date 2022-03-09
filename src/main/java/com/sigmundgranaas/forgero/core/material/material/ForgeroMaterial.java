package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroMaterial {
    int getRarity();

    @NotNull String getName();

    MaterialType getType();

    @NotNull List<Property> getProperties();

    String getIngredient();
}
