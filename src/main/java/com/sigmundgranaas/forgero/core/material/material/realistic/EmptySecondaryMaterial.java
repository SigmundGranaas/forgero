package com.sigmundgranaas.forgero.core.material.material.realistic;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EmptySecondaryMaterial implements SecondaryMaterial {
    @Override
    public int getRarity() {
        return 0;
    }

    @Override
    public String getName() {
        return "empty";
    }


    @Override
    public MaterialType getType() {
        return null;
    }

    @Override
    public List<String> getProperties() {
        return null;
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
}
