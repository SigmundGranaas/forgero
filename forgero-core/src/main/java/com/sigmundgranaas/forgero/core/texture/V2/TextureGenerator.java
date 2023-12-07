package com.sigmundgranaas.forgero.core.texture.V2;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.model.PaletteTemplateModel;

public class TextureGenerator {

	public static TextureGenerator INSTANCE;

	private final TextureService service;

	public TextureGenerator(FileLoader loader, Map<String, String> paletteRemap) {
		this.service = new TextureService(loader, paletteRemap);
	}

	public static TextureGenerator getInstance(FileLoader loader, Map<String, String> paletteRemap) {
		if (INSTANCE == null) {
			INSTANCE = new TextureGenerator(loader, paletteRemap);
		}
		return INSTANCE;
	}

	public Optional<JsonObject> getMetadata(PaletteTemplateModel templateModel, String extension) {
		var palette = service.getPaletteMetadata(templateModel.palette(), extension);
		var template = service.getTemplateMetadata(templateModel.template(), extension);
		if (palette.isPresent() || template.isPresent()) {
			return palette.isPresent() ? palette : template;
		}
		return Optional.empty();
	}

	public Optional<Texture> getTexture(PaletteTemplateModel templateModel) {
		var palette = service.getPalette(templateModel.palette() + ".png");
		var template = service.getTemplate(templateModel.template());
		if (palette.isPresent() && template.isPresent()) {
			var texture = new RawTexture(template.get().apply(palette.get()).getImage());
			if (ForgeroConfigurationLoader.configuration.exportGeneratedTextures) {
				saveImage(texture, templateModel.name());
			}
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
