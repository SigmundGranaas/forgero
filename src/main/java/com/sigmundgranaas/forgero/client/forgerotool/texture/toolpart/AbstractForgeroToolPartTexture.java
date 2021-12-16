package com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractForgeroToolPartTexture implements ForgeroToolPartTexture {
    protected TemplateTexture template;
    protected MaterialPalette palette;

    public AbstractForgeroToolPartTexture(TemplateTexture template, MaterialPalette palette) {
        this.template = template;
        this.palette = palette;
    }

    @NotNull
    public abstract BufferedImage performRecolour();

    @Override
    public @NotNull InputStream getToolPartTexture() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(performRecolour(), "PNG", os);
        return new ByteArrayInputStream(os.toByteArray());
    }
}
