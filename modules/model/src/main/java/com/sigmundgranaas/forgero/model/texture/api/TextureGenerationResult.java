package com.sigmundgranaas.forgero.model.texture.api;

import com.sigmundgranaas.forgero.model.texture.dto.AnimationMetadataDTO;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * The result of generating a texture, which may include animation metadata.
 *
 * @param image    The generated texture image. For animated textures, this contains
 *                 all frames stacked vertically.
 * @param metadata Animation metadata if the texture is animated, empty otherwise.
 */
public record TextureGenerationResult(
		BufferedImage image,
		Optional<AnimationMetadataDTO> metadata
) {
	/**
	 * Creates a result for a static (non-animated) texture.
	 *
	 * @param image The generated image.
	 * @return A result with no animation metadata.
	 */
	public static TextureGenerationResult staticResult(BufferedImage image) {
		return new TextureGenerationResult(image, Optional.empty());
	}

	/**
	 * Creates a result for an animated texture.
	 *
	 * @param image    The generated image containing all frames.
	 * @param metadata The animation metadata.
	 * @return A result with animation metadata.
	 */
	public static TextureGenerationResult animatedResult(BufferedImage image, AnimationMetadataDTO metadata) {
		return new TextureGenerationResult(image, Optional.of(metadata));
	}

	/**
	 * Checks if this result represents an animated texture.
	 *
	 * @return true if animation metadata is present, false otherwise.
	 */
	public boolean isAnimated() {
		return metadata.isPresent();
	}
}
