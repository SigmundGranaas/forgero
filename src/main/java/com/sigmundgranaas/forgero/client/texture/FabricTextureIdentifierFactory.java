package com.sigmundgranaas.forgero.client.texture;

import com.sigmundgranaas.forgero.client.forgerotool.model.ModelLayer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.TextureIdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartModelTextureIdentifier;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;

import java.util.Locale;
import java.util.Optional;

/**
 * Factory for creating texture identifiers from Minecraft Identifiers.
 * This class is not a part of the core library because its Identifiers needs an instance of Minecraft
 * <p>
 * <p>
 * This class needs another rework for properly identifying textures that need creation, and excluding textures that don't need it.
 */
public class FabricTextureIdentifierFactory implements TextureIdentifierFactory {
    @Override
    public ToolPartModelTextureIdentifier createToolPartTextureIdentifier(ForgeroToolPart part) {
        return new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getResourceName(), ToolPartModelType.getModelType(part), ModelLayer.PRIMARY, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER);
    }

    @Override
    public Optional<ToolPartModelTextureIdentifier> createToolPartTextureIdentifier(String part) {
        String[] elements = part.split(ToolPartModelTextureIdentifier.DEFAULT_SPLIT_OPERATOR);
        if (!ToolPartModelType.isModelIdentifier(elements) || elements.length <= 1) {
            return Optional.empty();
        }
        elements[0] = elements[0].split("/")[elements[0].split("/").length - 1];
        if (elements.length == 3) {
            elements = new String[]{elements[0], elements[1], "primary", elements[2]};
        }

        if (elements.length == ToolPartModelTextureIdentifier.DEFAULT_SPLIT_IDENTIFIER_LENGTH) {
            elements[0] = elements[0].split("/")[elements[0].split("/").length - 1];
            elements[ToolPartModelTextureIdentifier.SKIN_INDEX] = elements[ToolPartModelTextureIdentifier.SKIN_INDEX].replace(".png", "");
            return Optional.of(createIdentifier(elements));
        }

        return Optional.empty();
    }

    private ToolPartModelTextureIdentifier createIdentifier(String[] elements) {
        ToolPartModelType type = ToolPartModelType.valueOf(elements[ToolPartModelTextureIdentifier.MODEL_TYPE_INDEX].toUpperCase(Locale.ROOT));
        ModelLayer layer = ModelLayer.valueOf(elements[ToolPartModelTextureIdentifier.MODEL_LAYER_INDEX].toUpperCase(Locale.ROOT));
        String material = elements[ToolPartModelTextureIdentifier.MATERIAL_INDEX];
        String skin = elements[ToolPartModelTextureIdentifier.SKIN_INDEX];
        return new ToolPartModelTextureIdentifier(material, type, layer, skin);
    }
}
