package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticSecondaryMaterial;
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
    List<PaletteResourceIdentifier> getPaletteIdentifiers() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull
    List<PaletteResourceIdentifier> getPaletteExclusionIdentifiers() {
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

    @Override
    public int getMiningLevel() {
        return 0;
    }

    @Override
    public float getMiningSpeedAddition() {
        return 0;
    }

    @Override
    public int getAttackDamageAddition() {
        return 0;
    }

    @Override
    public float getAttackSpeedAddition() {
        return 0;
    }
}
