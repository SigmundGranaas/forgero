package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;

public class ResourceIdentifier {

    private PaletteIdentifier paletteIdentifier;
    private String resource;

    public ResourceIdentifier(PaletteIdentifier paletteIdentifier, String resource) {
        this.paletteIdentifier = paletteIdentifier;
        this.resource = resource;
    }


    public PaletteIdentifier getPaletteIdentifier() {
        return paletteIdentifier;
    }

    public String getResource() {
        return resource;
    }
}
