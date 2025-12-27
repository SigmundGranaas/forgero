package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.texture.TextureEntry;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.util.function.Supplier;

/**
 * Implementation of TextureEntry.
 */
public class TextureEntryImpl implements TextureEntry {

	private final Identifier id;
	private final BufferedImage image;
	private final boolean animated;
	private final Supplier<BufferedImage> imageSupplier;
	private final AnimationMeta animationMeta;

	public TextureEntryImpl(Identifier id, BufferedImage image, boolean animated, Supplier<BufferedImage> imageSupplier) {
		this(id, image, animated, imageSupplier, null);
	}

	public TextureEntryImpl(Identifier id, BufferedImage image, boolean animated, Supplier<BufferedImage> imageSupplier, AnimationMeta animationMeta) {
		this.id = id;
		this.image = image;
		this.animated = animated;
		this.imageSupplier = imageSupplier;
		this.animationMeta = animationMeta;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public BufferedImage getImage() {
		if (image != null) {
			return image;
		}
		if (imageSupplier != null) {
			return imageSupplier.get();
		}
		return null;
	}

	@Override
	public boolean isAnimated() {
		return animated;
	}

	@Override
	public AnimationMeta getAnimationMeta() {
		return animationMeta;
	}
}
