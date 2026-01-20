package com.sigmundgranaas.forgero.model.rendering.impl;

import com.sigmundgranaas.forgero.model.api.RenderableTexture;
import com.sigmundgranaas.forgero.model.rendering.api.TextureCompositor;
import com.sigmundgranaas.forgero.utility.resource.loader.api.UnifiedTextureProvider;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AwtTextureCompositor implements TextureCompositor {

	private final UnifiedTextureProvider textureProvider;
	public AwtTextureCompositor(UnifiedTextureProvider textureProvider) { this.textureProvider = textureProvider; }

	@Override
	public BufferedImage render(List<RenderableTexture> texturesToRender) {
		List<BufferedImage> images = new ArrayList<>();
		int width = 0, height = 0;

		// Sort textures by their order before rendering (RenderableTexture implements Comparable)
		texturesToRender.sort(Comparator.naturalOrder());

		for (RenderableTexture renderableTexture : texturesToRender) {
			Optional<BufferedImage> imageOpt = textureProvider.getTexture(renderableTexture.texture());
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

		// Draw images with their respective offsets
		for (RenderableTexture renderableTexture : texturesToRender) {
			Optional<BufferedImage> imageOpt = textureProvider.getTexture(renderableTexture.texture());
			imageOpt.ifPresent(image -> g.drawImage(image, renderableTexture.offset().x(), renderableTexture.offset().y(), null));
		}

		g.dispose();
		return canvas;
	}
}
