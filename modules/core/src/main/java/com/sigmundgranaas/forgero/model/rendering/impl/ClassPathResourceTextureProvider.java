package com.sigmundgranaas.forgero.model.rendering.impl;

import com.sigmundgranaas.forgero.model.rendering.api.TextureProvider;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Optional;

public class ClassPathResourceTextureProvider implements TextureProvider {
	@Override
	public Optional<BufferedImage> getTexture(String identifier) {
		String[] parts = identifier.split(":");
		if (parts.length != 2) return Optional.empty();
		String path = "/assets/" + parts[0] + "/textures/" + parts[1] + ".png";
		try (InputStream stream = getClass().getResourceAsStream(path)) {
			if (stream == null) {
				System.err.println("Texture not found: " + path);
				return Optional.empty();
			}
			return Optional.of(ImageIO.read(stream));
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
}
