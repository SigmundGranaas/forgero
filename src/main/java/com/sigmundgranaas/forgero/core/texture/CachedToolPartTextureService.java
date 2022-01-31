package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartTextureIdentifier;
import com.sigmundgranaas.forgero.core.texture.palette.*;
import com.sigmundgranaas.forgero.core.texture.palette.strategy.RecolourStrategyFactory;
import com.sigmundgranaas.forgero.core.texture.template.CachedTemplateTextureService;
import com.sigmundgranaas.forgero.core.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.core.texture.template.TemplateTextureFactory;
import com.sigmundgranaas.forgero.core.texture.template.TemplateTextureService;

public class CachedToolPartTextureService implements com.sigmundgranaas.forgero.core.texture.ToolPartTextureService {
    public static CachedToolPartTextureService INSTANCE;
    private final PaletteService paletteService;
    private final TemplateTextureService templateService;
    private final RecolourStrategyFactory recolourStrategyFactory;

    public CachedToolPartTextureService(PaletteService paletteService, TemplateTextureService templateTextureService, RecolourStrategyFactory recolourStrategyFactory) {
        this.paletteService = paletteService;
        this.templateService = templateTextureService;
        this.recolourStrategyFactory = recolourStrategyFactory;
    }

    public static CachedToolPartTextureService getInstance(TextureLoader loader) {
        if (INSTANCE == null) {
            TemplateTextureFactory templateTextureFactory = new TemplateTextureFactory();
            RecolourStrategyFactory strategyFactory = new RecolourStrategyFactory();
            PaletteFactory paletteFactory = new PaletteFactoryImpl();
            PaletteService paletteService = new CachedPaletteService(loader, paletteFactory);
            TemplateTextureService templateTextureService = new CachedTemplateTextureService(loader, templateTextureFactory);
            INSTANCE = new CachedToolPartTextureService(paletteService, templateTextureService, strategyFactory);
        }
        return INSTANCE;
    }

    @Override
    public Texture getTexture(ToolPartTextureIdentifier id) {
        Palette palette = paletteService.getPalette(id.getPaletteIdentifier());
        TemplateTexture template = templateService.getTemplate(id.getTemplateTextureIdentifier());
        RecolourStrategy strategy = recolourStrategyFactory.createStrategy(template, palette);
        return new RawTexture(id, strategy.recolour());
    }
}
