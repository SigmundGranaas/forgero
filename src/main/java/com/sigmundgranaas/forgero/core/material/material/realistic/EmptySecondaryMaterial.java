package com.sigmundgranaas.forgero.core.material.material.realistic;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleSecondaryMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EmptySecondaryMaterial implements SecondaryMaterial, RealisticSecondaryMaterial, SimpleSecondaryMaterial {
    @Override
    public int getRarity() {
        return 0;
    }

    @Override
    public @NotNull String getName() {
        return "empty";
    }


    @Override
    public MaterialType getType() {
        return null;
    }

    @Override
    public @NotNull List<String> getProperties() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull
    List<PaletteIdentifier> getPaletteIdentifiers() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull
    List<PaletteIdentifier> getPaletteExclusionIdentifiers() {
        return Collections.emptyList();
    }

    @Override
    public int getDurability() {
        return 0;
    }


    @Override
    public String getIngredient() {
        return "empty";
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
