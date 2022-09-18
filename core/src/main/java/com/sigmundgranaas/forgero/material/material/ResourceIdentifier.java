package com.sigmundgranaas.forgero.material.material;

import com.sigmundgranaas.forgero.identifier.texture.toolpart.PaletteIdentifier;

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
