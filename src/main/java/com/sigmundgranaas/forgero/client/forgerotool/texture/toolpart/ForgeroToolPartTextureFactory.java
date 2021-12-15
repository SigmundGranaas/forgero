package com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureIdentifier;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;

public interface ForgeroToolPartTextureFactory {
    static ForgeroToolPartTextureFactory createFactory() {
        return new ForgeroToolPartTextureFactoryImpl();
    }

    ForgeroToolPartTexture createToolPartTexture(TemplateTexture base, MaterialPalette palette, ForgeroTextureIdentifier identifier);
}
