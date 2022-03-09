package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;

public record ResourceIdentifier(
        PaletteIdentifier paletteIdentifier,
        String resource) {


    public PaletteIdentifier getPaletteIdentifier() {
        return paletteIdentifier;
    }

    public String getResource() {
        return resource;
    }
}
