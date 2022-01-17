package com.sigmundgranaas.forgero.core.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifier;

public class SecondaryMaterialTextureIdentifier extends BaseTextureIdentifier {
    public SecondaryMaterialTextureIdentifier(ToolPartModelType modelType, ForgeroMaterialIdentifier material) {
        super(modelType, material);
    }

    @Override
    public String getIdentifier() {
        return super.getIdentifier() + "_secondary";
    }

    @Override
    public TemplateTextureIdentifier getTemplateTextureIdentifier() {
        return new SecondaryTemplateTextureIdentifier(getToolPartModelType());
    }
}
