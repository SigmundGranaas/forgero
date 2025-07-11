package com.sigmundgranaas.forgero.model.texture.api;

import java.awt.image.BufferedImage;

/**
 * A service for writing a {@link BufferedImage} to a file.
 */
public interface TextureWriter {
	/**
	 * Saves an image to the specified path.
	 *
	 * @param image The image to save.
	 * @param path  The full destination path, including the filename and extension.
	 */
	void write(BufferedImage image, String path);
}
