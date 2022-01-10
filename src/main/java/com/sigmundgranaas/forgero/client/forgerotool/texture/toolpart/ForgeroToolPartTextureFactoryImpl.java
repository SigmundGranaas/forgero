package com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart.head.PickaxeHeadTexture;
import com.sigmundgranaas.forgero.core.identifier.model.texture.TextureIdentifier;

public class ForgeroToolPartTextureFactoryImpl implements ForgeroToolPartTextureFactory {
    @Override
    public ForgeroToolPartTexture createToolPartTexture(TemplateTexture base, MaterialPalette palette, TextureIdentifier identifier) {
        return switch (identifier.getModelType().get()) {
            case PICKAXEHEAD -> new PickaxeHeadTexture(base, palette);
            default -> new ForgeroDefaultTexture(base, palette);
        };
    }
}
