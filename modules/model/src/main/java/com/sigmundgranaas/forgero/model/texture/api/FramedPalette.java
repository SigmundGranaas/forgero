package com.sigmundgranaas.forgero.model.texture.api;

import java.awt.image.BufferedImage;

/**
 * Represents a palette that may contain multiple frames for animation.
 * Each frame is a single row of colors in the palette image.
 */
public interface FramedPalette {
	/**
	 * Gets the palette row for a specific frame.
	 * The returned image is a single-row image containing the palette colors.
	 *
	 * @param index The 0-based frame index.
	 * @return The palette row as a 1-pixel-tall BufferedImage.
	 * @throws IndexOutOfBoundsException if the index is out of range.
	 */
	BufferedImage getPaletteRow(int index);

	/**
	 * Gets the total number of palette frames (rows).
	 *
	 * @return The frame count (always >= 1).
	 */
	int getFrameCount();

	/**
	 * Gets the width of the palette (number of colors per row).
	 *
	 * @return The palette width in pixels.
	 */
	int getPaletteWidth();

	/**
	 * Gets the full source palette image.
	 *
	 * @return The complete palette image.
	 */
	BufferedImage getSourceImage();

	/**
	 * Checks if this palette contains multiple frames (is animated).
	 *
	 * @return true if frame count > 1, false otherwise.
	 */
	default boolean isAnimated() {
		return getFrameCount() > 1;
	}
}
