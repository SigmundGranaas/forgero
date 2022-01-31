package com.sigmundgranaas.forgero.core.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifier;

public class BaseTextureIdentifier extends ToolPartTextureIdentifier {

    private final ToolPartModelType modelType;
    private final ForgeroMaterialIdentifier material;

    public BaseTextureIdentifier(ToolPartModelType modelType, ForgeroMaterialIdentifier material) {
        this.modelType = modelType;
        this.material = material;
    }


    @Override
    public ToolPartModelType getToolPartModelType() {
        return modelType;
    }

    @Override
    public ForgeroMaterialIdentifier getMaterial() {
        return material;
    }

}
