package com.sigmundgranaas.forgero.core.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;

public class TemplateTextureIdentifier implements TextureIdentifier {

    private final ToolPartModelType toolPartModelType;

    public TemplateTextureIdentifier(ToolPartModelType toolPartModelType) {
        this.toolPartModelType = toolPartModelType;
    }

    @Override
    public String getFileNameWithExtension() {
        return getIdentifier() + ".png";
    }

    @Override
    public String getFileNameWithoutExtension() {
        return getIdentifier();
    }

    @Override
    public String getIdentifier() {
        return toolPartModelType.toFileName() + "_base";
    }

    public ToolPartModelType getToolPartModelType() {
        return this.toolPartModelType;
    }
}
