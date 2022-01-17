package com.sigmundgranaas.forgero.core.texture.palette;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.texture.Texture;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class UnbakedMaterialPalette implements UnbakedPalette {
    private final PaletteIdentifier id;
    private final List<Texture> inclusions;
    private final List<Texture> exclusions;

    public UnbakedMaterialPalette(PaletteIdentifier id, List<Texture> inclusions, List<Texture> exclusions) {
        this.id = id;
        this.inclusions = inclusions;
        this.exclusions = exclusions;
    }

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
