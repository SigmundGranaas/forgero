package com.sigmundgranaas.forgero.core.texture.palette;

import com.sigmundgranaas.forgero.core.material.material.PaletteResourceIdentifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PaletteResourceRegistry {
    public static PaletteResourceRegistry INSTANCE;
    private final Map<String, PaletteResourceIdentifier> paletteIdentifierMap;

    private PaletteResourceRegistry() {
        paletteIdentifierMap = new HashMap<>();
    }

    public static PaletteResourceRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PaletteResourceRegistry();
        }
        return INSTANCE;
    }

    public void addPalette(PaletteResourceIdentifier palette) {
        this.paletteIdentifierMap.put(palette.getIdentifier(), palette);
    }

    public Optional<PaletteResourceIdentifier> getPalette(String identifier) {
        return Optional.ofNullable(paletteIdentifierMap.get(identifier));
    }
}
