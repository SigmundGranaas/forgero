package com.sigmundgranaas.forgerocore.texture.palette.strategy;

import com.sigmundgranaas.forgerocore.deprecated.ModelLayer;
import com.sigmundgranaas.forgerocore.texture.palette.Palette;
import com.sigmundgranaas.forgerocore.texture.palette.RecolourStrategy;
import com.sigmundgranaas.forgerocore.texture.template.TemplateTexture;

/**
 * Factory for choosing which strategy should be applied to any given tool parts
 */
public class RecolourStrategyFactory {
    public RecolourStrategy createStrategy(TemplateTexture template, Palette palette) {
        if (palette.getColourValues().isEmpty()) {
            return new EmptyRecolourStrategy(template);
        }
        if (template.getId().getToolPartModelLayer() == ModelLayer.SECONDARY) {
            return switch (template.getId().getToolPartModelType()) {
                case BINDING -> new SecondaryToolPartRecolourStrategy(template, palette);
                case PICKAXE_BINDING -> new SecondaryToolPartRecolourStrategy(template, palette);
                case SHOVEL_BINDING -> new SecondaryToolPartRecolourStrategy(template, palette);
                case PICKAXEHEAD -> new PickaxeHeadRecolourStrategy(template, palette);
                case SHOVELHEAD -> new PickaxeHeadRecolourStrategy(template, palette);
                default -> new DefaultToolPartRecolourStrategy(template, palette);
            };
        } else if (template.getId().getToolPartModelLayer() == ModelLayer.GEM) {
            return switch (template.getId().getToolPartModelType()) {
                case PICKAXEHEAD -> new SecondaryToolPartRecolourStrategy(template, palette);
                case SHOVELHEAD -> new SecondaryToolPartRecolourStrategy(template, palette);
                case AXEHEAD -> new SecondaryToolPartRecolourStrategy(template, palette);
                default -> new DefaultToolPartRecolourStrategy(template, palette);
            };
        }
        return switch (template.getId().getToolPartModelType()) {
            case PICKAXEHEAD -> new PickaxeHeadRecolourStrategy(template, palette);
            case SHOVELHEAD -> new PickaxeHeadRecolourStrategy(template, palette);
            case AXEHEAD -> new PickaxeHeadRecolourStrategy(template, palette);
            default -> new DefaultToolPartRecolourStrategy(template, palette);
        };
    }
}
