package com.sigmundgranaas.forgero.model.texture.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.generation.api.TextureGenerationTask;
import com.sigmundgranaas.forgero.model.texture.api.PalettizedTextureGenerator;
import com.sigmundgranaas.forgero.model.texture.api.TextureGenerator;
import com.sigmundgranaas.forgero.model.texture.api.TextureWriter;
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

	public DefaultTextureGenerator(ResourceProvider resourceProvider, PalettizedTextureGenerator palettizedGenerator, TextureWriter textureWriter) {
		this.resourceProvider = resourceProvider;
		this.palettizedGenerator = palettizedGenerator;
		this.textureWriter = textureWriter;
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

				// 3. Generate the final image (can throw validation exceptions)
				BufferedImage finalImage = palettizedGenerator.generate(templateOpt.get(), paletteOpt.get());

				// 4. Write the final image to a file
				String filePath = "assets/" + task.output().replace(":", "/") + ".png";
				textureWriter.write(finalImage, filePath);

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
