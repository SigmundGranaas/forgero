package com.sigmundgranaas.forgero.model.texture.impl;

import com.sigmundgranaas.forgero.model.texture.api.*;
import com.sigmundgranaas.forgero.model.texture.dto.AnimationMetadataDTO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Default implementation of animated texture generation.
 * Handles all four animation scenarios and produces properly formatted output.
 * <p>
 * Uses an existing {@link PalettizedTextureGenerator} to process individual frames,
 * then combines them into a vertical strip with appropriate metadata.
 */
public class DefaultAnimatedPalettizedTextureGenerator implements AnimatedPalettizedTextureGenerator {
	private final PalettizedTextureGenerator frameGenerator;

	/**
	 * Creates a new animated generator with the specified single-frame generator.
	 *
	 * @param frameGenerator The generator to use for individual frame processing.
	 */
	public DefaultAnimatedPalettizedTextureGenerator(PalettizedTextureGenerator frameGenerator) {
		this.frameGenerator = frameGenerator;
	}

	@Override
	public TextureGenerationResult generate(
			FramedTexture template,
			FramedPalette palette,
			Optional<AnimationMetadataDTO> templateMetadata,
			Optional<AnimationMetadataDTO> paletteMetadata
	) {
		int templateFrames = template.getFrameCount();
		int paletteFrames = palette.getFrameCount();

		// Case 1: Both single frame - static output
		if (templateFrames == 1 && paletteFrames == 1) {
			BufferedImage result = generateSingleFrame(template, palette, 0, 0);
			return TextureGenerationResult.staticResult(result);
		}

		// Animated output - determine total frames and process
		int totalFrames = Math.max(templateFrames, paletteFrames);
		int frameSize = template.getFrameSize();

		// Create output image with all frames stacked vertically
		BufferedImage result = new BufferedImage(
				frameSize,
				frameSize * totalFrames,
				BufferedImage.TYPE_INT_ARGB
		);
		Graphics2D g = result.createGraphics();

		// Process each frame
		for (int frameIndex = 0; frameIndex < totalFrames; frameIndex++) {
			int templateIndex = selectFrameIndex(frameIndex, templateFrames);
			int paletteIndex = selectFrameIndex(frameIndex, paletteFrames);

			BufferedImage frameImage = generateSingleFrame(template, palette, templateIndex, paletteIndex);
			g.drawImage(frameImage, 0, frameIndex * frameSize, null);
		}
		g.dispose();

		// Build output metadata - template takes precedence
		AnimationMetadataDTO outputMetadata = buildOutputMetadata(templateMetadata, paletteMetadata);

		return TextureGenerationResult.animatedResult(result, outputMetadata);
	}

	/**
	 * Generates a single frame by applying one palette row to one template frame.
	 */
	private BufferedImage generateSingleFrame(
			FramedTexture template,
			FramedPalette palette,
			int templateIndex,
			int paletteIndex
	) {
		BufferedImage templateFrame = template.getFrame(templateIndex);
		BufferedImage paletteRow = palette.getPaletteRow(paletteIndex);
		return frameGenerator.generate(templateFrame, paletteRow);
	}

	/**
	 * Selects which frame to use when the frame count is less than the total.
	 * Uses modulo wrapping to cycle through available frames.
	 */
	private int selectFrameIndex(int outputFrame, int availableFrames) {
		if (availableFrames == 1) {
			return 0; // Static - always use first frame
		}
		return outputFrame % availableFrames; // Cycle through available frames
	}

	/**
	 * Builds output metadata. Template metadata takes precedence when available.
	 */
	private AnimationMetadataDTO buildOutputMetadata(
			Optional<AnimationMetadataDTO> templateMetadata,
			Optional<AnimationMetadataDTO> paletteMetadata
	) {
		// Template takes precedence
		if (templateMetadata.isPresent()) {
			return templateMetadata.get();
		}

		// Fall back to palette metadata
		if (paletteMetadata.isPresent()) {
			return paletteMetadata.get();
		}

		// Default animation settings
		return AnimationMetadataDTO.DEFAULT;
	}
}
