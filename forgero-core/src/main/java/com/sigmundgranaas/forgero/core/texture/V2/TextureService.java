package com.sigmundgranaas.forgero.core.texture.V2;

import com.sigmundgranaas.forgero.core.texture.V2.recolor.DefaultRecolorStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TextureService {
    public static String PALETTE_PATH = "assets/forgero/templates/materials/";
    public static String TEMPLATE_PATH = "assets/forgero/templates/textures/";
    private final TextureLoader loader;
    private Map<String, Palette> paletteCache;
    private Map<String, TemplateTexture> templateCache;
    private Map<String, Palette> previousPaletteCache;
    private Map<String, TemplateTexture> previousTemplateCache;

    public TextureService(FileLoader loader) {
        this.paletteCache = new HashMap<>();
        this.templateCache = new HashMap<>();
        this.previousPaletteCache = new HashMap<>();
        this.previousTemplateCache = new HashMap<>();
        this.loader = new TextureLoader(loader);
    }

    public Optional<Palette> getPalette(String name) {
        if (paletteCache.containsKey(name)) {
            return Optional.of(paletteCache.get(name));
        }
        var template = loader.load(PALETTE_PATH + name).or(() -> Optional.ofNullable(previousTemplateCache.get(name).getImage()));
        template.ifPresent(palette -> paletteCache.put(name, new Palette(palette)));
        return template.map(Palette::new);
    }

    public void clear() {
        previousPaletteCache = paletteCache;
        previousTemplateCache = templateCache;

        templateCache = new ConcurrentHashMap<>();
        paletteCache = new ConcurrentHashMap<>();
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
