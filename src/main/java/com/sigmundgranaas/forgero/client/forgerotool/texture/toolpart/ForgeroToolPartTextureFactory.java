package com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureIdentifier;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;

/**
 * Factory for creating ForgeroToolPartTexture Objects
 */
public interface ForgeroToolPartTextureFactory {
    static ForgeroToolPartTextureFactory createFactory() {
        return new ForgeroToolPartTextureFactoryImpl();
    }

    /**
     * Abstract factory method for creating A ToolPartTexture
     *
     * @param base       - TemplateTexture defining the shape of the Texture
     * @param palette    MaterialPalette defining the available colours to pick from
     * @param identifier - Identifier for recognising which type of should be created
     * @return A ForgeroToolPartTexture corresponding to the correct type, and with the appropriate materials.
     */
    ForgeroToolPartTexture createToolPartTexture(TemplateTexture base, MaterialPalette palette, ForgeroTextureIdentifier identifier);
}
