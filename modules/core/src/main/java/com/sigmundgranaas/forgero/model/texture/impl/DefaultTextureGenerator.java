package com.sigmundgranaas.forgero.model.texture.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.generation.api.TextureGenerationTask;
import com.sigmundgranaas.forgero.model.texture.api.*;
import com.sigmundgranaas.forgero.model.texture.dto.AnimationMetadataDTO;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class DefaultTextureGenerator implements TextureGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTextureGenerator.class);
	private final ResourceProvider resourceProvider;
	private final PalettizedTextureGenerator palettizedGenerator;
	private final TextureWriter textureWriter;
	private final AnimationDetector animationDetector;
	private final AnimatedPalettizedTextureGenerator animatedGenerator;

	/**
	 * Creates a texture generator with animation support.
	 */
	public DefaultTextureGenerator(ResourceProvider resourceProvider, PalettizedTextureGenerator palettizedGenerator, TextureWriter textureWriter) {
		this.resourceProvider = resourceProvider;
		this.palettizedGenerator = palettizedGenerator;
		this.textureWriter = textureWriter;
		this.animationDetector = new AnimationDetector(resourceProvider);
		this.animatedGenerator = new DefaultAnimatedPalettizedTextureGenerator(palettizedGenerator);
	}

	/**
	 * Creates a texture generator with custom animation components.
	 */
	public DefaultTextureGenerator(
			ResourceProvider resourceProvider,
			PalettizedTextureGenerator palettizedGenerator,
			TextureWriter textureWriter,
			AnimationDetector animationDetector,
			AnimatedPalettizedTextureGenerator animatedGenerator
	) {
		this.resourceProvider = resourceProvider;
		this.palettizedGenerator = palettizedGenerator;
		this.textureWriter = textureWriter;
		this.animationDetector = animationDetector;
		this.animatedGenerator = animatedGenerator;
	}

	@Override
	public void generate(List<TextureGenerationTask> tasks) {
		for (TextureGenerationTask task : tasks) {
			try {
				// 1. Load template image
				Optional<BufferedImage> templateOpt = loadImage(task.template());
				if (templateOpt.isEmpty()) {
					LOGGER.error("Aborting generation for '{}' because required template '{}' could not be loaded", task.output(), task.template());
					continue;
				}

				// 2. Load palette image
				Optional<BufferedImage> paletteOpt = loadImage(task.palette());
				if (paletteOpt.isEmpty()) {
					LOGGER.error("Aborting generation for '{}' because required palette '{}' could not be loaded", task.output(), task.palette());
					continue;
				}

				// 3. Detect animation metadata for template and palette
				Optional<AnimationMetadataDTO> templateMetadata = animationDetector.detectAnimation(task.template());
				Optional<AnimationMetadataDTO> paletteMetadata = animationDetector.detectAnimation(task.palette());

				// 4. Generate the final image with animation support
				String filePath = "assets/" + task.output().replace(":", "/") + ".png";

				if (templateMetadata.isPresent() || paletteMetadata.isPresent() || isMultiFrame(templateOpt.get()) || isMultiFrame(paletteOpt.get())) {
					// Use animated generation pipeline
					generateAnimated(templateOpt.get(), paletteOpt.get(), templateMetadata, paletteMetadata, filePath);
				} else {
					// Use simple single-frame generation
					BufferedImage finalImage = palettizedGenerator.generate(templateOpt.get(), paletteOpt.get());
					textureWriter.write(finalImage, filePath);
				}

			} catch (IllegalArgumentException e) {
				// Catches strict validation errors from AwtPalettizedTextureGenerator
				LOGGER.error("Validation failed while generating texture for '{}'. Task: (Template: {}, Palette: {}). Reason: {}",
						task.output(), task.template(), task.palette(), e.getMessage());
			} catch (Exception e) {
				// Generic catch-all for other unexpected errors during the process
				LOGGER.error("An unexpected error occurred while generating texture for '{}'. Task: (Template: {}, Palette: {})",
						task.output(), task.template(), task.palette(), e);
			}
		}
	}

	/**
	 * Checks if an image appears to be multi-frame (height is a multiple of width and greater than width).
	 */
	private boolean isMultiFrame(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		return height > width && height % width == 0;
	}

	/**
	 * Generates an animated texture using the framed texture pipeline.
	 */
	private void generateAnimated(
			BufferedImage templateImage,
			BufferedImage paletteImage,
			Optional<AnimationMetadataDTO> templateMetadata,
			Optional<AnimationMetadataDTO> paletteMetadata,
			String filePath
	) {
		// Wrap images in framed texture/palette wrappers
		FramedTexture template = VerticalStripFramedTexture.fromImage(templateImage);
		FramedPalette palette = RowBasedFramedPalette.fromImage(paletteImage);

		// Generate with animation support
		TextureGenerationResult result = animatedGenerator.generate(template, palette, templateMetadata, paletteMetadata);

		// Write using animated writer if texture writer supports it, otherwise fall back
		if (textureWriter instanceof AnimatedTextureWriter animatedWriter) {
			animatedWriter.write(result, filePath);
		} else {
			// Fall back: write image only, no mcmeta
			textureWriter.write(result.image(), filePath);
			if (result.isAnimated()) {
				LOGGER.warn("Animation metadata for '{}' will be lost because TextureWriter does not support animation", filePath);
			}
		}
	}

	private Optional<BufferedImage> loadImage(String identifier) {
		String[] parts = identifier.split(":", 2);
		if (parts.length != 2) {
			LOGGER.error("Invalid texture identifier format: '{}'. Expected 'namespace:path'", identifier);
			return Optional.empty();
		}
		String namespace = parts[0];
		String path = parts[1];

		// Handle inconsistent root directory for templates/palettes.
		// Standard textures are expected to be in the "textures" directory.
		if (!path.startsWith("textures/") && !path.startsWith("texture_templates/") && !path.startsWith("palettes/")) {
			path = "textures/" + path;
		}

		String fullPath = path.endsWith(".png") ? path : path + ".png";
		OpenIdentifier imageId = new OpenIdentifier(namespace, fullPath);

		try {
			Optional<InputStream> streamOpt = resourceProvider.read(imageId);

			if (streamOpt.isEmpty()) {
				LOGGER.error("Image resource not found at path '{}' from original identifier '{}'", imageId, identifier);
				return Optional.empty();
			}

			try (InputStream stream = streamOpt.get()) {
				BufferedImage image = ImageIO.read(stream);
				if (image == null) {
					LOGGER.error("Failed to decode image file at path '{}'. The file might be corrupted or in an unsupported format", imageId);
					return Optional.empty();
				}
				return Optional.of(image);
			}

		} catch (IOException e) {
			LOGGER.error("An I/O error occurred while reading image resource '{}'. Reason: {}", imageId, e.getMessage());
			return Optional.empty();
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while loading image resource '{}'", imageId, e);
			return Optional.empty();
		}
	}
}
