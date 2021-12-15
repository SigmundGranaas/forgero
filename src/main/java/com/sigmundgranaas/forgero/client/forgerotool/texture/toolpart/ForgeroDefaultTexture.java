package com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

public class ForgeroDefaultTexture extends AbstractForgeroToolPartTexture {
    public ForgeroDefaultTexture(TemplateTexture template, MaterialPalette palette) {
        super(template, palette);
    }

    @Override
    public @NotNull BufferedImage performRecolour() {
        return template.createRecolouredImage(palette);
    }
}
