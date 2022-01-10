package com.sigmundgranaas.forgero.client.forgerotool.texture.template.factory;

import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.core.identifier.model.texture.TextureIdentifier;

public class OverridableTemplateTextureFactory extends TemplateTextureFactoryImpl {
    @Override
    protected void saveTemplateTexture(TextureIdentifier textureIdentifier, TemplateTexture baseTexture) {
        super.saveTemplateTexture(textureIdentifier, baseTexture);
        super.WriteTemplateTexturePaletteAsImage(textureIdentifier, baseTexture);
    }
}
