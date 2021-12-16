package com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart.head;

import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart.ForgeroDefaultTexture;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

public class PickaxeHeadTexture extends ForgeroDefaultTexture {
    public PickaxeHeadTexture(TemplateTexture template, MaterialPalette palette) {
        super(template, palette);
    }

    @Override
    public @NotNull BufferedImage performRecolour() {
        return super.performRecolour();
    }

}
