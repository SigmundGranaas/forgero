package com.sigmundgranaas.forgero.item.forgerotool.material.material;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Material {
    int getRarity();

    String getName();

    int getDurability();

    int getWeight();

    MaterialType getType();

    List<String> getProperties();

    @NotNull List<Identifier> getPaletteIdentifiers();

    @NotNull List<Identifier> getPaletteExclusionIdentifiers();
}
