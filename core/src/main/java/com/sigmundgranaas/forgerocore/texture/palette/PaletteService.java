package com.sigmundgranaas.forgerocore.texture.palette;

import com.sigmundgranaas.forgerocore.identifier.texture.toolpart.PaletteIdentifier;

/**
 * Service for fetching Palettes. Palettes will be created or fetched from disk when they are requested.
 */
public interface PaletteService {
    Palette getPalette(PaletteIdentifier id);

    void clearCache();
}
