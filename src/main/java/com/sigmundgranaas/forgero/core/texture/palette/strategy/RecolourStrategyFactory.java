package com.sigmundgranaas.forgero.core.texture.palette.strategy;

import com.sigmundgranaas.forgero.core.texture.palette.Palette;
import com.sigmundgranaas.forgero.core.texture.palette.RecolourStrategy;
import com.sigmundgranaas.forgero.core.texture.template.TemplateTexture;

public class RecolourStrategyFactory {
    public RecolourStrategy createStrategy(TemplateTexture template, Palette palette) {
        //More branches will be added
        //noinspection SwitchStatementWithTooFewBranches
        return switch (template.getId().getToolPartModelType()) {
            default -> new DefaultToolPartRecolourStrategy(template, palette);
        };
    }
}
