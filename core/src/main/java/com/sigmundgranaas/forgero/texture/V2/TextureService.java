package com.sigmundgranaas.forgero.texture.V2;

import com.sigmundgranaas.forgero.texture.V2.recolor.DefaultRecolorStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TextureService {
    public static String PALETTE_PATH = "assets/forgero/templates/materials/";
    public static String TEMPLATE_PATH = "assets/forgero/templates/textures/";
    private final Map<String, Palette> paletteCache;
    private final Map<String, TemplateTexture> templateCache;
    private final TextureLoader loader;

    public TextureService(FileLoader loader) {
        this.paletteCache = new HashMap<>();
        this.templateCache = new HashMap<>();
        this.loader = new TextureLoader(loader);
    }

    public Optional<Palette> getPalette(String name) {
        if (paletteCache.containsKey(name)) {
            return Optional.of(paletteCache.get(name));
        }
        var template = loader.load(PALETTE_PATH + name);
        template.ifPresent(palette -> paletteCache.put(name, new Palette(palette)));
        return template.map(Palette::new);
    }

    public Optional<TemplateTexture> getTemplate(String name) {
        if (templateCache.containsKey(name)) {
            return Optional.of(templateCache.get(name));
        }
        var templateOpt = loader.load(TEMPLATE_PATH + name);
        if (templateOpt.isPresent()) {
            var texture = new TemplateTexture(templateOpt.get(), new DefaultRecolorStrategy());
            templateCache.put(name, texture);
            return Optional.of(texture);
        }
        return Optional.empty();
    }
}
