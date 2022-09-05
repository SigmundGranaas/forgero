package com.sigmundgranaas.forgerocore.texture.palette;

import com.sigmundgranaas.forgerocore.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgerocore.texture.Texture;

import java.util.List;

/**
 * A UnbakedPalette stores all necessary information to create a palette, listing which textures should be included, and which should be excluded from the final palette.
 */
public interface UnbakedPalette {
    Palette bake();

    PaletteIdentifier getId();

    List<Texture> getInclusions();

    List<Texture> getExclusions();
}
