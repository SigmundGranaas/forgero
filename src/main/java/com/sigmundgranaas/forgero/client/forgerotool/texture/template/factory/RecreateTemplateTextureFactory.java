package com.sigmundgranaas.forgero.client.forgerotool.texture.template.factory;

import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.client.forgerotool.texture.utils.TextureLoader;
import com.sigmundgranaas.forgero.core.identifier.model.texture.TextureIdentifier;

public class RecreateTemplateTextureFactory extends TemplateTextureFactoryImpl {

    @Override
    protected void saveTemplateTexture(TextureIdentifier textureIdentifier, TemplateTexture baseTexture) {
        super.saveTemplateTexture(textureIdentifier, baseTexture);
        saveTemplateTextureIfNotExists(textureIdentifier, baseTexture);
    }

    protected void saveTemplateTextureIfNotExists(TextureIdentifier textureIdentifier, TemplateTexture baseTexture) {
        if (!templateTexturePaletteExists(textureIdentifier)) {
            WriteTemplateTexturePaletteAsImage(textureIdentifier, baseTexture);
        }
    }

    private boolean templateTexturePaletteExists(TextureIdentifier textureIdentifier) {
        return TextureLoader.textureExists(getTemplateTextureFolder() + "palette_" + textureIdentifier.getBaseTextureFileNameWithExtension());
    }
}
