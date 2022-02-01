package com.sigmundgranaas.forgero.core.material.material.realistic;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.material.material.AbstractForgeroMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PrimaryMaterialImpl extends AbstractForgeroMaterial implements RealisticPrimaryMaterial {
    private final String ingredient;

    public PrimaryMaterialImpl(RealisticMaterialPOJO material) {
        super(material);
        ingredient = material.ingredient.item;
    }

    @Override
    public int getRarity() {
        return 0;
    }

    @Override
    public @NotNull
    List<PaletteIdentifier> getPaletteIdentifiers() {
        return super.getPaletteIdentifiers();
    }

    @Override
    public @NotNull
    List<PaletteIdentifier> getPaletteExclusionIdentifiers() {
        return super.getPaletteExclusionIdentifiers();
    }


    @Override
    public int getDurability() {
        return 0;
    }


    @Override
    public int getStiffness() {
        return 0;
    }

    @Override
    public int getSharpness() {
        return 0;
    }

    @Override
    public String getIngredient() {
        return ingredient;
    }

}
