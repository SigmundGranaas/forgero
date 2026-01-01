package com.sigmundgranaas.forgero.model.texture.api;

import com.sigmundgranaas.forgero.model.texture.dto.AnimationMetadataDTO;

import java.util.Optional;

/**
 * A service that generates textures from templates and palettes with animation support.
 * Handles all four animation scenarios:
 * <ol>
 *   <li>Static template + Static palette → Static output</li>
 *   <li>Animated template + Static palette → Animated output</li>
 *   <li>Static template + Animated palette → Animated output</li>
 *   <li>Animated template + Animated palette → Animated output (synchronized)</li>
 * </ol>
 */
public interface AnimatedPalettizedTextureGenerator {
	/**
	 * Generates a texture by applying a palette to a template, handling animation
	 * if either input contains multiple frames.
	 *
	 * @param template         The template texture (may have multiple frames).
	 * @param palette          The color palette (may have multiple frames).
	 * @param templateMetadata Animation metadata for the template, if available.
	 * @param paletteMetadata  Animation metadata for the palette, if available.
	 * @return The generation result containing the image and any animation metadata.
	 */
	TextureGenerationResult generate(
			FramedTexture template,
			FramedPalette palette,
			Optional<AnimationMetadataDTO> templateMetadata,
			Optional<AnimationMetadataDTO> paletteMetadata
	);
}
