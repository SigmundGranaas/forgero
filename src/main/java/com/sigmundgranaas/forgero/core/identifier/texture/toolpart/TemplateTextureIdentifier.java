package com.sigmundgranaas.forgero.core.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.ModelLayer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;

public record TemplateTextureIdentifier(
        ToolPartModelType toolPartModelType,
        ModelLayer layer,
        String skin
) implements TextureIdentifier {

    @Override
    public String getFileNameWithExtension() {
        return getFileNameWithoutExtension() + ".png";
    }

    @Override
    public String getFileNameWithoutExtension() {
        return String.format("%s_%s", toolPartModelType.toFileName(), layer.getFileName());
    }

    @Override
    public String getIdentifier() {
        return String.format("%s_%s_%s", toolPartModelType.toFileName(), layer.getFileName(), skin);
    }

    @Override
    public String skin() {
        return skin;
    }

    public ToolPartModelType getToolPartModelType() {
        return this.toolPartModelType;
    }

    public ModelLayer getToolPartModelLayer() {
        return this.layer;
    }
}
