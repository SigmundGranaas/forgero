package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.material.material.realistic.MaterialType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroMaterial {
    int getRarity();

    @NotNull String getName();

    int getDurability();

    MaterialType getType();

    @NotNull List<String> getProperties();

    String getIngredient();

    @NotNull
    List<PaletteResourceIdentifier> getPaletteIdentifiers();

    @NotNull
    List<PaletteResourceIdentifier> getPaletteExclusionIdentifiers();
}
