package com.sigmundgranaas.forgero.client.forgerotool.texture.template.factory;

import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureIdentifier;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.client.forgerotool.texture.utils.TextureLoader;

public class RecreateTemplateTextureFactory extends TemplateTextureFactoryImpl {

    @Override
    protected void saveTemplateTexture(ForgeroTextureIdentifier textureIdentifier, TemplateTexture baseTexture) {
        super.saveTemplateTexture(textureIdentifier, baseTexture);
        saveTemplateTextureifNotExists(textureIdentifier, baseTexture);
    }

    protected void saveTemplateTextureifNotExists(ForgeroTextureIdentifier textureIdentifier, TemplateTexture baseTexture) {
        if (!templateTexturePaletteExists(textureIdentifier)) {
            WriteTemplateTexturePaletteAsImage(textureIdentifier, baseTexture);
        }
    }

    private boolean templateTexturePaletteExists(ForgeroTextureIdentifier textureIdentifier) {
        return TextureLoader.textureExists(getTemplateTextureFolder() + "palette_" + textureIdentifier.getBaseTextureFileNameWithExtension());
    }


}
