package com.sigmundgranaas.forgero.core.texture.V2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.texture.V2.recolor.DefaultRecolorStrategy;

public class TextureService {
	public static String PALETTE_PATH = "assets/forgero/templates/materials/";
	public static String TEMPLATE_PATH = "assets/forgero/templates/textures/";
	private final TextureLoader loader;
	private final Map<String, String> paletteRemap;
	private Map<String, Palette> paletteCache;
	private Map<String, TemplateTexture> templateCache;

	public TextureService(FileLoader loader, Map<String, String> paletteRemap) {
		this.paletteCache = new HashMap<>();
		this.paletteRemap = paletteRemap;
		this.templateCache = new HashMap<>();
		this.loader = new TextureLoader(loader);
	}

	public Optional<Palette> getPalette(String name) {
		var remapped = Optional.ofNullable(paletteRemap.get(name)).flatMap(this::getPalette);
		if (remapped.isPresent()) {
			return remapped;
		}
		if (paletteCache.containsKey(name)) {
			return Optional.ofNullable(paletteCache.get(name));
		}
		var paletteTexture = loader.load(PALETTE_PATH + name);
		var paletteOpt = paletteTexture.map(Palette::new);
		if (paletteOpt.isPresent()) {
			var palette = paletteOpt.get();
			if (palette.getColourValues().size() >= 2) {
				paletteCache.put(name, palette);
				return Optional.of(palette);
			} else {
				Forgero.LOGGER.error("Encountered Palette texture: {} with a limited number of color values: {}. This palette will not be processed.", PALETTE_PATH + name, palette.getColourValues().size());
			}
		}
		return Optional.empty();
	}

	public void clear() {
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
			if (texture.getGreyScaleValues().size() >= 2) {
				templateCache.put(name, texture);
				return Optional.of(texture);
			}
			Forgero.LOGGER.error("Encountered template texture: {} with a limited number of grayscale values: {}. This texture will not be processed.", TEMPLATE_PATH + name, texture.getGreyScaleValues().size());
		}
		return Optional.empty();
	}
}
