package com.sigmundgranaas.forgero.model.texture.api;

import com.sigmundgranaas.forgero.model.texture.dto.AnimationMetadataDTO;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * A texture writer that supports animated textures by writing both the PNG image
 * and the accompanying .mcmeta metadata file when animation is present.
 */
public interface AnimatedTextureWriter extends TextureWriter {
	/**
	 * Writes a texture with optional animation metadata.
	 * If metadata is present, writes both the PNG file and a .mcmeta file.
	 *
	 * @param image    The image to write.
	 * @param path     The output path for the PNG file.
	 * @param metadata Optional animation metadata. If present, a .mcmeta file will be created.
	 */
	void write(BufferedImage image, String path, Optional<AnimationMetadataDTO> metadata);

	/**
	 * Convenience method to write a TextureGenerationResult.
	 *
	 * @param result The generation result containing image and optional metadata.
	 * @param path   The output path for the PNG file.
	 */
	default void write(TextureGenerationResult result, String path) {
		write(result.image(), path, result.metadata());
	}
}
