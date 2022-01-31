package com.sigmundgranaas.forgero.core.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifier;

public abstract class ToolPartTextureIdentifier implements TextureIdentifier {

    @Override
    public String getIdentifier() {
        return this.getMaterial().getName() + "_" + getToolPartModelType().toFileName();
    }

    public abstract ToolPartModelType getToolPartModelType();

    public abstract ForgeroMaterialIdentifier getMaterial();


    @Override
    public String getFileNameWithExtension() {
        return this.getFileNameWithoutExtension() + ".png";
    }

    @Override
    public String getFileNameWithoutExtension() {
        return this.getIdentifier();
    }

    public TemplateTextureIdentifier getTemplateTextureIdentifier() {
        return new TemplateTextureIdentifier(getToolPartModelType());
    }

    public PaletteIdentifier getPaletteIdentifier() {
        return new PaletteIdentifier(getMaterial());
    }
}
