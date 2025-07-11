package com.sigmundgranaas.forgero.model.texture.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.generation.api.TextureGenerationTask;
import com.sigmundgranaas.forgero.model.texture.api.PalettizedTextureGenerator;
import com.sigmundgranaas.forgero.model.texture.api.TextureGenerator;
import com.sigmundgranaas.forgero.model.texture.api.TextureWriter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class DefaultTextureGenerator implements TextureGenerator {
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
					System.err.printf("ERROR: Aborting generation for '%s' because required template '%s' could not be loaded.%n", task.output(), task.template());
					continue;
				}

				// 2. Load palette image
				Optional<BufferedImage> paletteOpt = loadImage(task.palette());
				if (paletteOpt.isEmpty()) {
					System.err.printf("ERROR: Aborting generation for '%s' because required palette '%s' could not be loaded.%n", task.output(), task.palette());
					continue;
				}

				// 3. Generate the final image (can throw validation exceptions)
				BufferedImage finalImage = palettizedGenerator.generate(templateOpt.get(), paletteOpt.get());

				// 4. Write the final image to a file
				String filePath = "assets/" + task.output().replace(":", "/") + ".png";
				textureWriter.write(finalImage, filePath);

				// System.out.println("INFO: Successfully generated texture: " + task.output());

			} catch (IllegalArgumentException e) {
				// Catches strict validation errors from AwtPalettizedTextureGenerator
				System.err.printf("ERROR: Validation failed while generating texture for '%s'. Task: (Template: %s, Palette: %s). Reason: %s%n",
						task.output(), task.template(), task.palette(), e.getMessage());
			} catch (Exception e) {
				// Generic catch-all for other unexpected errors during the process
				System.err.printf("ERROR: An unexpected error occurred while generating texture for '%s'. Task: (Template: %s, Palette: %s).%n",
						task.output(), task.template(), task.palette());
				e.printStackTrace();
			}
		}
	}

	private Optional<BufferedImage> loadImage(String identifier) {
		// Ensure the identifier correctly resolves to a .png file path
		String resourceIdentifier = identifier.endsWith(".png") ? identifier : identifier + ".png";
		OpenIdentifier imageId = new OpenIdentifier(resourceIdentifier);

		try {
			Optional<InputStream> streamOpt = resourceProvider.read(imageId);

			if (streamOpt.isEmpty()) {
				System.err.printf("ERROR: Image resource not found at path '%s'%n", imageId);
				return Optional.empty();
			}

			try (InputStream stream = streamOpt.get()) {
				BufferedImage image = ImageIO.read(stream);
				if (image == null) {
					System.err.printf("ERROR: Failed to decode image file at path '%s'. The file might be corrupted or in an unsupported format.%n", imageId);
					return Optional.empty();
				}
				return Optional.of(image);
			}

		} catch (IOException e) {
			System.err.printf("ERROR: An I/O error occurred while reading image resource '%s'. Reason: %s%n", imageId, e.getMessage());
			return Optional.empty();
		} catch (Exception e) {
			System.err.printf("ERROR: An unexpected error occurred while loading image resource '%s'.%n", imageId);
			e.printStackTrace();
			return Optional.empty();
		}
	}
}
