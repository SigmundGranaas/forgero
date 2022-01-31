package com.sigmundgranaas.forgero.core.texture.palette;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.texture.Texture;

import java.util.List;

public interface UnbakedPalette {
    Palette bake();

    PaletteIdentifier getId();

    List<Texture> getInclusions();

    List<Texture> getExclusions();
}
