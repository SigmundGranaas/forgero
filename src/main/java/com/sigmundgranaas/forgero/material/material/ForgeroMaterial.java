package com.sigmundgranaas.forgero.material.material;

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

    @NotNull
    List<Identifier> getPaletteIdentifiers();

    @NotNull
    List<Identifier> getPaletteExclusionIdentifiers();
}
