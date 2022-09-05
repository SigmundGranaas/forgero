package com.sigmundgranaas.forgerocore.texture.palette;

import com.sigmundgranaas.forgerocore.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgerocore.texture.Texture;

import java.util.List;


public record UnbakedMaterialPalette(PaletteIdentifier id,
                                     List<Texture> inclusions,
                                     List<Texture> exclusions) implements UnbakedPalette {

    @Override
    public Palette bake() {
        return null;
    }

    @Override
    public PaletteIdentifier getId() {
        return id;
    }

    @Override
    public List<Texture> getInclusions() {
        return inclusions;
    }

    @Override
    public List<Texture> getExclusions() {
        return exclusions;
    }
}
