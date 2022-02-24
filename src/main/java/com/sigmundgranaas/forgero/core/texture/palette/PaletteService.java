package com.sigmundgranaas.forgero.core.texture.palette;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;

/**
 * Service for fetching Palettes. Palettes will be created or fetched from disk when they are requested.
 */
public interface PaletteService {
    Palette getPalette(PaletteIdentifier id);
}
