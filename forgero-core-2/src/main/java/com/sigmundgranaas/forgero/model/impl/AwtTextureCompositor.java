package com.sigmundgranaas.forgero.model.impl;

import com.sigmundgranaas.forgero.model.api.LayeredTexture;
import com.sigmundgranaas.forgero.model.api.TextureCompositor;
import com.sigmundgranaas.forgero.model.api.TextureLayer;
import com.sigmundgranaas.forgero.model.api.TextureProvider;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AwtTextureCompositor implements TextureCompositor {

	private final TextureProvider textureProvider;
	public AwtTextureCompositor(TextureProvider textureProvider) { this.textureProvider = textureProvider; }

	@Override
	public BufferedImage render(LayeredTexture model) {
		List<BufferedImage> images = new ArrayList<>();
		int width = 0, height = 0;
		for (TextureLayer layer : model.layers()) {
			Optional<BufferedImage> imageOpt = textureProvider.getTexture(layer.texture());
			if (imageOpt.isPresent()) {
				BufferedImage image = imageOpt.get();
				images.add(image);
				width = Math.max(width, image.getWidth());
				height = Math.max(height, image.getHeight());
			}
		}
		if (width == 0 || height == 0) { return new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB); }
		BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = canvas.createGraphics();
		for (BufferedImage image : images) { g.drawImage(image, 0, 0, null); }
		g.dispose();
		return canvas;
	}
}
