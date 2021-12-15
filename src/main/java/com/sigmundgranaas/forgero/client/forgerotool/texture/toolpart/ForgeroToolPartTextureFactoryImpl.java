package com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureIdentifier;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart.head.PickaxeHeadTexture;

public class ForgeroToolPartTextureFactoryImpl implements ForgeroToolPartTextureFactory {
    @Override
    public ForgeroToolPartTexture createToolPartTexture(TemplateTexture base, MaterialPalette palette, ForgeroTextureIdentifier identifier) {
        return switch (identifier.getModelType().get()) {
            case PICKAXEHEAD -> new PickaxeHeadTexture(base, palette);
            default -> new ForgeroDefaultTexture(base, palette);
        };
    }
}
