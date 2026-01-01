package com.sigmundgranaas.forgero.model.texture.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.model.texture.api.AnimatedTextureWriter;
import com.sigmundgranaas.forgero.model.texture.dto.AnimationMetadataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * File-based implementation of AnimatedTextureWriter.
 * Writes PNG files and accompanying .mcmeta files for animated textures.
 * Uses codec-based serialization consistent with the rest of the system.
 */
public class FileAnimatedTextureWriter implements AnimatedTextureWriter {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileAnimatedTextureWriter.class);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final String outputDir;

	/**
	 * Creates a new file writer.
	 *
	 * @param outputDir The root directory where files will be saved.
	 */
	public FileAnimatedTextureWriter(String outputDir) {
		this.outputDir = outputDir;
	}

	@Override
	public void write(BufferedImage image, String path) {
		write(image, path, Optional.empty());
	}

	@Override
	public void write(BufferedImage image, String path, Optional<AnimationMetadataDTO> metadata) {
		// Write the PNG file
		writePng(image, path);

		// Write the .mcmeta file if metadata is present
		metadata.ifPresent(dto -> writeMcmeta(path, dto));
	}

	private void writePng(BufferedImage image, String path) {
		try {
			File outputFile = new File(outputDir, path);
			createParentDirectories(outputFile);
			ImageIO.write(image, "PNG", outputFile);
		} catch (IOException e) {
			LOGGER.error("Failed to write PNG texture to path: {}", path, e);
		}
	}

	private void writeMcmeta(String pngPath, AnimationMetadataDTO metadata) {
		String mcmetaPath = pngPath + ".mcmeta";
		try {
			File mcmetaFile = new File(outputDir, mcmetaPath);
			createParentDirectories(mcmetaFile);

			// Use codec to serialize metadata to JSON
			JsonElement jsonElement = AnimationMetadataDTO.CODEC.encodeStart(JsonOps.INSTANCE, metadata)
					.resultOrPartial(error -> LOGGER.error("Failed to encode mcmeta: {}", error))
					.orElse(null);

			if (jsonElement != null) {
				String content = GSON.toJson(jsonElement);
				try (FileWriter writer = new FileWriter(mcmetaFile, StandardCharsets.UTF_8)) {
					writer.write(content);
				}
			}
		} catch (IOException e) {
			LOGGER.error("Failed to write mcmeta file to path: {}", mcmetaPath, e);
		}
	}

	private void createParentDirectories(File file) {
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			//noinspection ResultOfMethodCallIgnored
			parent.mkdirs();
		}
	}
}
