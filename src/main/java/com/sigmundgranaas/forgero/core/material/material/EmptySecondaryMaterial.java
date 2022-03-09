package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticSecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleSecondaryMaterial;
import com.sigmundgranaas.forgero.core.properties.Property;
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
    public @NotNull List<Property> getProperties() {
        return Collections.emptyList();
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
