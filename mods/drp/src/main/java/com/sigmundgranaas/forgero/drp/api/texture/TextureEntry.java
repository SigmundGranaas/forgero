package com.sigmundgranaas.forgero.drp.api.texture;

import com.sigmundgranaas.forgero.drp.impl.builder.TextureEntryImpl;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.util.function.Supplier;

/**
 * Represents a texture entry for the resource pack.
 */
public interface TextureEntry {

	/**
	 * Creates a texture entry from a BufferedImage.
	 *
	 * @param id    The texture identifier
	 * @param image The texture image
	 * @return A new texture entry
	 */
	static TextureEntry of(Identifier id, BufferedImage image) {
		return new TextureEntryImpl(id, image, false, null);
	}

	/**
	 * Creates a texture entry from a BufferedImage with the given path.
	 *
	 * @param namespace The texture namespace
	 * @param path      The texture path (e.g., "item/iron_blade")
	 * @param image     The texture image
	 * @return A new texture entry
	 */
	static TextureEntry of(String namespace, String path, BufferedImage image) {
		return of(new Identifier(namespace, path), image);
	}

	/**
	 * Creates a lazy-loaded texture entry.
	 * The supplier is only called when the texture is actually needed.
	 *
	 * @param id            The texture identifier
	 * @param imageSupplier Supplier that produces the image
	 * @return A new texture entry
	 */
	static TextureEntry lazy(Identifier id, Supplier<BufferedImage> imageSupplier) {
		return new TextureEntryImpl(id, null, false, imageSupplier);
	}

	/**
	 * Creates an animated texture entry.
	 *
	 * @param id            The texture identifier
	 * @param image         The texture image (containing all frames vertically)
	 * @param animationMeta The animation metadata
	 * @return A new texture entry
	 */
	static TextureEntry animated(Identifier id, BufferedImage image, AnimationMeta animationMeta) {
		return new TextureEntryImpl(id, image, true, null, animationMeta);
	}

	/**
	 * Gets the texture identifier.
	 *
	 * @return The texture id
	 */
	Identifier getId();

	/**
	 * Gets the texture image.
	 *
	 * @return The image data
	 */
	BufferedImage getImage();

	/**
	 * Gets whether this texture has animation metadata.
	 *
	 * @return true if animated
	 */
	boolean isAnimated();

	/**
	 * Gets the animation metadata if this is an animated texture.
	 *
	 * @return Animation metadata, or null if not animated
	 */
	AnimationMeta getAnimationMeta();

	/**
	 * Animation metadata for animated textures.
	 */
	interface AnimationMeta {
		/**
		 * Gets the frame time in game ticks.
		 */
		int getFrameTime();

		/**
		 * Whether to interpolate between frames.
		 */
		boolean isInterpolate();

		/**
		 * Gets specific frame indices, or null for sequential.
		 */
		int[] getFrames();

		/**
		 * Creates animation metadata with default settings.
		 *
		 * @param frameTime The frame time in ticks
		 * @return Animation metadata
		 */
		static AnimationMeta of(int frameTime) {
			return new AnimationMeta() {
				@Override
				public int getFrameTime() {
					return frameTime;
				}

				@Override
				public boolean isInterpolate() {
					return false;
				}

				@Override
				public int[] getFrames() {
					return null;
				}
			};
		}

		/**
		 * Creates animation metadata with interpolation.
		 *
		 * @param frameTime   The frame time in ticks
		 * @param interpolate Whether to interpolate
		 * @return Animation metadata
		 */
		static AnimationMeta of(int frameTime, boolean interpolate) {
			return new AnimationMeta() {
				@Override
				public int getFrameTime() {
					return frameTime;
				}

				@Override
				public boolean isInterpolate() {
					return interpolate;
				}

				@Override
				public int[] getFrames() {
					return null;
				}
			};
		}
	}
}
