package com.sigmundgranaas.forgero.model.texture.api;

import java.awt.image.BufferedImage;

/**
 * A pure, stateless service that generates a colored texture from a greyscale template and a color palette.
 */
public interface PalettizedTextureGenerator {
	/**
	 * Recolors a greyscale template image using a palette.
	 * The logic maps the grey value of each pixel in the template to the
	 * x-coordinate of the palette to pick a color.
	 *
	 * @param template The greyscale source image.
	 * @param palette  The palette image. It should be 256 pixels wide.
	 * @return A new, colored {@link BufferedImage}.
	 */
	BufferedImage generate(BufferedImage template, BufferedImage palette);
}
