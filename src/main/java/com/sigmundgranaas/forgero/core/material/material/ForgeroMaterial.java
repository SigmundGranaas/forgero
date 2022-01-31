package com.sigmundgranaas.forgero.core.material.material;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroMaterial {
    int getRarity();

    String getName();

    int getDurability();

    int getWeight();

    MaterialType getType();

    List<String> getProperties();

    String getIngredientAsString();

    @NotNull
    List<Identifier> getPaletteIdentifiers();

    @NotNull
    List<Identifier> getPaletteExclusionIdentifiers();
}
