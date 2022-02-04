package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;

public class PaletteResourceIdentifier {

    private PaletteIdentifier paletteIdentifier;
    private String resource;

    public PaletteResourceIdentifier(PaletteIdentifier paletteIdentifier, String resource) {
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
