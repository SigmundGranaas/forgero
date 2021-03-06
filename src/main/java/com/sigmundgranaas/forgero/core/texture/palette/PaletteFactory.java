package com.sigmundgranaas.forgero.core.texture.palette;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;

import java.awt.image.BufferedImage;

/**
 * Factory for generating palettes from sets of Images
 */
public interface PaletteFactory {
    Palette createPalette(UnbakedPalette palette);

    Palette createPalette(BufferedImage palette, PaletteIdentifier id);
}
