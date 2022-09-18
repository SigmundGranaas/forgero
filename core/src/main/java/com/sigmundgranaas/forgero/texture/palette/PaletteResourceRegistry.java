package com.sigmundgranaas.forgero.texture.palette;

import com.sigmundgranaas.forgero.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.material.material.PaletteResourceIdentifier;

import java.util.*;

/**
 * Registry for registering palettes and keeping track of which palettes should be generated, and which should be loaded from file.
 */
public class PaletteResourceRegistry {
    public static PaletteResourceRegistry INSTANCE;
    private final Map<String, PaletteResourceIdentifier> paletteIdentifierMap;
    private final Set<String> premadePalettes;

    private PaletteResourceRegistry() {
        this.premadePalettes = new HashSet<>();
        this.paletteIdentifierMap = new HashMap<>();
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

    public void addPremadePalette(String id) {
        premadePalettes.add(id);
    }

    public boolean premadePalette(PaletteIdentifier id) {
        return premadePalettes.contains(id.getIdentifier());
    }
}
