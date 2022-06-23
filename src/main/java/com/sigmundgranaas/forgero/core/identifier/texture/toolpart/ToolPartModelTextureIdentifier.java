package com.sigmundgranaas.forgero.core.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.ModelLayer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

public record ToolPartModelTextureIdentifier(String material,
                                             ToolPartModelType type,
                                             ModelLayer layer,
                                             String skin) implements TextureIdentifier {
    public static final String DEFAULT_SKIN_IDENTIFIER = "default";
    public static final int DEFAULT_SPLIT_IDENTIFIER_LENGTH = 4;
    public static final int MODEL_TYPE_INDEX = 1;
    public static final int MODEL_LAYER_INDEX = 2;
    public static final int MATERIAL_INDEX = 0;
    public static final int SKIN_INDEX = 3;
    public static final String DEFAULT_SPLIT_OPERATOR = "-";

    @Override
    public String getFileNameWithExtension() {
        return getFileNameWithoutExtension() + ".png";
    }

    @Override
    public String getFileNameWithoutExtension() {
        return String.format("%s%s%s%s%s%s%s", material, ELEMENT_SEPARATOR, type.toFileName(), ELEMENT_SEPARATOR, layer.getFileName(), ELEMENT_SEPARATOR, skin);
    }

    @Override
    public String getIdentifier() {
        return getFileNameWithoutExtension();
    }

    public TemplateTextureIdentifier getTemplateTextureIdentifier() {
        return new TemplateTextureIdentifier(type, layer, skin);
    }

    public PaletteIdentifier getPaletteIdentifier() {
        return new PaletteIdentifier(material);
    }

}
