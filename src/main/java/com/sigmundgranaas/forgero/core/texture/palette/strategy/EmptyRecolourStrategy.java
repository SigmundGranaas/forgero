package com.sigmundgranaas.forgero.core.texture.palette.strategy;

import com.sigmundgranaas.forgero.core.texture.palette.RecolourStrategy;
import com.sigmundgranaas.forgero.core.texture.template.TemplateTexture;

import java.awt.image.BufferedImage;

public class EmptyRecolourStrategy implements RecolourStrategy {
    private final TemplateTexture template;

    public EmptyRecolourStrategy(TemplateTexture template) {
        this.template = template;
    }

    @Override
    public BufferedImage recolour() {
        return template.getImage();
    }
}
