package com.sigmundgranaas.forgero.model.rendering.impl;

import com.sigmundgranaas.forgero.model.rendering.api.TextureProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Optional;

public class ClassPathResourceTextureProvider implements TextureProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathResourceTextureProvider.class);

	@Override
	public Optional<BufferedImage> getTexture(String identifier) {
		String[] parts = identifier.split(":");
		if (parts.length != 2) return Optional.empty();
		String path = "/assets/" + parts[0] + "/textures/" + parts[1] + ".png";
		try (InputStream stream = getClass().getResourceAsStream(path)) {
			if (stream == null) {
				LOGGER.debug("Texture not found: {}", path);
				return Optional.empty();
			}
			return Optional.of(ImageIO.read(stream));
		} catch (Exception e) {
			LOGGER.error("Failed to load texture: {}", path, e);
			return Optional.empty();
		}
	}
}
