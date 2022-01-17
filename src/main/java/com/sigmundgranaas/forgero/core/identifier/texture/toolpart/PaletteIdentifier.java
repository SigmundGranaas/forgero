package com.sigmundgranaas.forgero.core.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifier;

public class PaletteIdentifier implements TextureIdentifier {
    private final ForgeroMaterialIdentifier material;

    public PaletteIdentifier(ForgeroMaterialIdentifier material) {
        this.material = material;
    }

    @Override
    public String getFileNameWithExtension() {
        return getFileNameWithoutExtension() + ".png";
    }

    @Override
    public String getFileNameWithoutExtension() {
        return getIdentifier() + "_palette";
    }

    @Override
    public String getIdentifier() {
        return material.getName();
    }


}
