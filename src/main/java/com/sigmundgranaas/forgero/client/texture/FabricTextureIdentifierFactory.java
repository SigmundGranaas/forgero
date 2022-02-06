package com.sigmundgranaas.forgero.client.texture;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.BaseTextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.SecondaryMaterialTextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.TextureIdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartTextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import net.minecraft.util.Identifier;

import java.util.Locale;
import java.util.Optional;

public class FabricTextureIdentifierFactory implements TextureIdentifierFactory {
    @Override
    public ToolPartTextureIdentifier createToolPartTextureIdentifier(ForgeroToolPart part) {
        return new BaseTextureIdentifier(ToolPartModelType.getModelType(part), new ForgeroMaterialIdentifierImpl(part.getPrimaryMaterial().getName()));
    }

    @Override
    public Optional<ToolPartTextureIdentifier> createToolPartTextureIdentifier(Identifier part) {
        if (!part.getPath().contains(".png") || !part.getNamespace().equals(Forgero.MOD_NAMESPACE) || part.toString().contains("transparent_base") || part.toString().contains("gem")) {
            return Optional.empty();
        }
        String[] elements = part.getPath().split("/");
        String identifier = elements[elements.length - 1];
        elements[elements.length - 1] = elements[elements.length - 1].replace(".png", "");

        String[] identifierElements = identifier.split("_");
        identifierElements[identifierElements.length - 1] = identifierElements[identifierElements.length - 1].replace(".png", "");


        if (identifierElements.length == 2) {
            return Optional.of(createToolPartIdentifier(identifierElements));
        } else if (identifierElements.length == 3) {
            return Optional.of(createSecondaryToolPartIdentifier(identifierElements));
        }
        return Optional.empty();
    }

    private ToolPartTextureIdentifier createSecondaryToolPartIdentifier(String[] identifierElements) {
        return new SecondaryMaterialTextureIdentifier(ToolPartModelType.valueOf(identifierElements[1].toUpperCase(Locale.ROOT)), new ForgeroMaterialIdentifierImpl(identifierElements[0]));
    }

    ToolPartTextureIdentifier createToolPartIdentifier(String[] elements) {
        return new BaseTextureIdentifier(ToolPartModelType.valueOf(elements[1].toUpperCase(Locale.ROOT)), new ForgeroMaterialIdentifierImpl(elements[0]));
    }
}
