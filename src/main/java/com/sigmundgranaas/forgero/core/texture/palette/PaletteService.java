package com.sigmundgranaas.forgero.core.texture.palette;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;

public interface PaletteService {
    Palette getPalette(PaletteIdentifier id);
}
