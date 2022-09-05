package com.sigmundgranaas.forgerocore.identifier.texture.toolpart;

import com.sigmundgranaas.forgerocore.identifier.texture.TextureIdentifier;

public record PaletteIdentifier(
        String material) implements TextureIdentifier {

    @Override
    public String getFileNameWithExtension() {
        return getFileNameWithoutExtension() + ".png";
    }

    @Override
    public String getFileNameWithoutExtension() {
        return getIdentifier();
    }

    @Override
    public String getIdentifier() {
        return material;
    }


}
