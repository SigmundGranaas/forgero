package com.sigmundgranaas.forgero.core.texture.palette.strategy;

import com.sigmundgranaas.forgero.client.forgerotool.model.ModelLayer;
import com.sigmundgranaas.forgero.core.texture.palette.Palette;
import com.sigmundgranaas.forgero.core.texture.palette.RecolourStrategy;
import com.sigmundgranaas.forgero.core.texture.template.TemplateTexture;

public class RecolourStrategyFactory {
    public RecolourStrategy createStrategy(TemplateTexture template, Palette palette) {

        if (template.getId().getToolPartModelLayer() == ModelLayer.SECONDARY) {
            return switch (template.getId().getToolPartModelType()) {
                case BINDING -> new SecondaryToolPartRecolourStrategy(template, palette);
                case PICKAXEBINDING -> new SecondaryToolPartRecolourStrategy(template, palette);
                case SHOVELBINDING -> new SecondaryToolPartRecolourStrategy(template, palette);
                case PICKAXEHEAD -> new PickaxeHeadRecolourStrategy(template, palette);
                case SHOVELHEAD -> new PickaxeHeadRecolourStrategy(template, palette);
                default -> new DefaultToolPartRecolourStrategy(template, palette);
            };
        }
        return switch (template.getId().getToolPartModelType()) {
            case PICKAXEHEAD -> new PickaxeHeadRecolourStrategy(template, palette);
            case SHOVELHEAD -> new PickaxeHeadRecolourStrategy(template, palette);
            default -> new DefaultToolPartRecolourStrategy(template, palette);
        };
    }
}
