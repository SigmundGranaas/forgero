package com.sigmundgranaas.forgerocore.material.material;

import com.sigmundgranaas.forgerocore.identifier.texture.toolpart.PaletteIdentifier;

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
