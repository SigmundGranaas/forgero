package com.sigmundgranaas.forgero.client.forgerotool.texture.template.factory;

import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureIdentifier;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;

public class OverridableTemplateTextureFactory extends TemplateTextureFactoryImpl {
    @Override
    protected void saveTemplateTexture(ForgeroTextureIdentifier textureIdentifier, TemplateTexture baseTexture) {
        super.saveTemplateTexture(textureIdentifier, baseTexture);
        super.WriteTemplateTexturePaletteAsImage(textureIdentifier, baseTexture);
    }
}
