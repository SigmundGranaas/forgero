package com.sigmundgranaas.forgero.core.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.ModelLayer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

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
        return ForgeroRegistry.SCHEMATIC.getResource(skin + "-schematic").map(schematic -> schematic.getModelContainer().getModel(toolPartModelType).getModel(layer)).orElse("missing");
        //return String.format("%s%s%s", toolPartModelType.toFileName(), ELEMENT_SEPARATOR, layer.getFileName());
    }

    @Override
    public String getIdentifier() {
        return String.format("%s%s%s%s%s", toolPartModelType.toFileName(), ELEMENT_SEPARATOR, layer.getFileName(), ELEMENT_SEPARATOR, skin);
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
