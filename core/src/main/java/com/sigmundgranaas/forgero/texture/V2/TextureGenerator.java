package com.sigmundgranaas.forgero.texture.V2;


import com.sigmundgranaas.forgero.model.PaletteTemplateModel;

import java.util.Optional;

public class TextureGenerator {

    public static TextureGenerator INSTANCE;

    private final TextureService service;

    public TextureGenerator(FileLoader loader) {
        this.service = new TextureService(loader);
    }

    public static TextureGenerator getInstance(FileLoader loader) {
        if (INSTANCE == null) {
            INSTANCE = new TextureGenerator(loader);
        }
        return INSTANCE;
    }

    public Optional<Texture> getTexture(PaletteTemplateModel templateModel) {
        var palette = service.getPalette(templateModel.palette() + ".png");
        var template = service.getTemplate(templateModel.template());
        if (palette.isPresent() && template.isPresent()) {
            return Optional.of(new RawTexture(template.get().apply(palette.get()).getImage()));
        }

        return Optional.empty();
    }
}
