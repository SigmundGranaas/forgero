package com.sigmundgranaas.forgero.core.texture.V2;


import com.sigmundgranaas.forgero.core.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.core.Forgero;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
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
            var texture = new RawTexture(template.get().apply(palette.get()).getImage());
            //saveImage(texture, templateModel.name());
            return Optional.of(texture);
        }

        return Optional.empty();
    }

    public void clear() {
        service.clear();
    }

    private void saveImage(RawTexture texture, String name) {
        var outputPath = "./export/generated_textures/";
        File outputFile = new File(outputPath + name);
        if (!outputFile.exists()) {
            outputFile.mkdirs();
        }
        try {
            ImageIO.write(texture.image(), "png", outputFile);
            Forgero.LOGGER.info("exported: {}", outputFile.toString());
        } catch (IOException e) {
            Forgero.LOGGER.error(e);
        }

    }
}
