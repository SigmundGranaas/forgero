package com.sigmundgranaas.forgero.core.texture.V2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.texture.V2.recolor.DefaultRecolorStrategy;

public class TextureService {
	public static String PALETTE_PATH = "assets/forgero/templates/materials/";
	public static String TEMPLATE_PATH = "assets/forgero/templates/textures/";
	private final TextureLoader loader;
	private final Map<String, String> paletteRemap;
	private final Map<String, JsonObject> metaCache;
	private final Set<String> attemptedTextures;
	private Map<String, Palette> paletteCache;
	private Map<String, TemplateTexture> templateCache;


	public TextureService(FileLoader loader, Map<String, String> paletteRemap) {
		this.paletteCache = new HashMap<>();
		this.paletteRemap = paletteRemap;
		this.templateCache = new HashMap<>();
		this.metaCache = new HashMap<>();
		this.attemptedTextures = new HashSet<>();
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
			if (palette.getColourValues(0).size() >= 2) {
				paletteCache.put(name, palette);
				return Optional.of(palette);
			} else {
				Forgero.LOGGER.error("Encountered Palette texture: {} with a limited number of color values: {}. This palette will not be processed.", PALETTE_PATH + name, palette.getColourValues(0).size());
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
			if (texture.getGreyScaleValues(0).size() >= 2) {
				templateCache.put(name, texture);
				return Optional.of(texture);
			}
			Forgero.LOGGER.error("Encountered template texture: {} with a limited number of grayscale values: {}. This texture will not be processed.", TEMPLATE_PATH + name, texture.getGreyScaleValues(0).size());
		}
		return Optional.empty();
	}

	public Optional<JsonObject> getPaletteMetadata(String texture, String extension) {
		return getMetadata(PALETTE_PATH + texture + ".png" + extension);
	}

	public Optional<JsonObject> getTemplateMetadata(String texture, String extension) {
		return getMetadata(TEMPLATE_PATH + texture + extension);
	}

	public Optional<JsonObject> getMetadata(String name) {
		if (attemptedTextures.contains(name)) {
			return Optional.empty();
		}
		if (metaCache.containsKey(name)) {
			return Optional.ofNullable(metaCache.get(name));
		} else {
			var meta = loader.loadMetadata(name);
			if (meta.isPresent()) {
				metaCache.put(name, meta.get());
				return meta;
			}

			attemptedTextures.add(name);

		}
		return Optional.empty();
	}
}
