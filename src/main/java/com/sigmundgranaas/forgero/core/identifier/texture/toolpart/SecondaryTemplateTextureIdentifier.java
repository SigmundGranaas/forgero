package com.sigmundgranaas.forgero.core.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;

public class SecondaryTemplateTextureIdentifier extends TemplateTextureIdentifier {
    public SecondaryTemplateTextureIdentifier(ToolPartModelType toolPartModelType) {
        super(toolPartModelType);
    }

    @Override
    public String getIdentifier() {
        return super.getIdentifier() + "_secondary";
    }


}
