package com.sigmundgranaas.forgero.model.texture.api;

import java.awt.image.BufferedImage;

/**
 * Represents a texture that may contain multiple animation frames.
 * Frames are assumed to be square and stacked vertically in the source image.
 */
public interface FramedTexture {
	/**
	 * Gets a specific frame from the texture.
	 *
	 * @param index The 0-based frame index.
	 * @return The frame as a BufferedImage.
	 * @throws IndexOutOfBoundsException if the index is out of range.
	 */
	BufferedImage getFrame(int index);

	/**
	 * Gets the total number of frames in this texture.
	 *
	 * @return The frame count (always >= 1).
	 */
	int getFrameCount();

	/**
	 * Gets the size (width and height) of each frame.
	 * Frames are square, so this is both the width and height.
	 *
	 * @return The frame size in pixels.
	 */
	int getFrameSize();

	/**
	 * Gets the full source image containing all frames.
	 *
	 * @return The complete texture image.
	 */
	BufferedImage getSourceImage();

	/**
	 * Checks if this texture contains multiple frames (is animated).
	 *
	 * @return true if frame count > 1, false otherwise.
	 */
	default boolean isAnimated() {
		return getFrameCount() > 1;
	}
}
