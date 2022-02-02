package com.sigmundgranaas.forgero.core.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;

@SuppressWarnings("ClassCanBeRecord")
public class PaletteTemplateIdentifier implements TextureIdentifier {
    private final String id;


    public PaletteTemplateIdentifier(String id) {
        this.id = id;
    }

    @Override
    public String getFileNameWithExtension() {
        return id + ".png";
    }

    @Override
    public String getFileNameWithoutExtension() {
        return id;
    }

    @Override
    public String getIdentifier() {
        return id;
    }
}
