package com.sigmundgranaas.forgero.model.texture.impl;

import com.sigmundgranaas.forgero.model.texture.api.TextureWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileTextureWriter implements TextureWriter {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileTextureWriter.class);
	private final String outputDir;

	/**
	 * @param outputDir The root directory where textures will be saved (e.g., "build/generated/resources/").
	 */
	public FileTextureWriter(String outputDir) {
		this.outputDir = outputDir;
	}

	@Override
	public void write(BufferedImage image, String path) {
		try {
			File outputFile = new File(outputDir, path);
			//noinspection ResultOfMethodCallIgnored
			outputFile.getParentFile().mkdirs();
			ImageIO.write(image, "PNG", outputFile);
		} catch (IOException e) {
			LOGGER.error("Failed to write texture to path: {}", path, e);
		}
	}
}
