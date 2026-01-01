package com.sigmundgranaas.forgero.model.texture.impl;

import com.sigmundgranaas.forgero.model.texture.api.FramedTexture;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A framed texture implementation that extracts frames from a vertical texture strip.
 * Each frame is a square region with size equal to the image width.
 * Frames are stacked vertically from top to bottom.
 * <p>
 * For example, a 16x48 image contains 3 frames of 16x16 pixels each.
 */
public class VerticalStripFramedTexture implements FramedTexture {
	private final BufferedImage sourceImage;
	private final int frameSize;
	private final List<BufferedImage> frames;

	/**
	 * Creates a framed texture from a source image.
	 * The frame size is determined by the image width.
	 *
	 * @param sourceImage The source image containing vertically stacked frames.
	 * @throws IllegalArgumentException if the image dimensions are invalid.
	 */
	public VerticalStripFramedTexture(BufferedImage sourceImage) {
		if (sourceImage == null) {
			throw new IllegalArgumentException("Source image cannot be null");
		}

		this.sourceImage = sourceImage;
		this.frameSize = sourceImage.getWidth();
		this.frames = new ArrayList<>();

		if (frameSize <= 0) {
			throw new IllegalArgumentException("Image width must be positive");
		}

		if (sourceImage.getHeight() % frameSize != 0) {
			throw new IllegalArgumentException(String.format(
					"Image height (%d) must be a multiple of width (%d) for vertical strip format",
					sourceImage.getHeight(), frameSize
			));
		}

		extractFrames();
	}

	private void extractFrames() {
		int frameCount = sourceImage.getHeight() / frameSize;
		for (int i = 0; i < frameCount; i++) {
			BufferedImage frame = sourceImage.getSubimage(0, i * frameSize, frameSize, frameSize);
			// Create a copy to avoid issues with subimage references
			BufferedImage frameCopy = new BufferedImage(frameSize, frameSize, BufferedImage.TYPE_INT_ARGB);
			frameCopy.getGraphics().drawImage(frame, 0, 0, null);
			frames.add(frameCopy);
		}
	}

	@Override
	public BufferedImage getFrame(int index) {
		if (index < 0 || index >= frames.size()) {
			throw new IndexOutOfBoundsException(String.format(
					"Frame index %d out of bounds [0, %d)", index, frames.size()
			));
		}
		return frames.get(index);
	}

	@Override
	public int getFrameCount() {
		return frames.size();
	}

	@Override
	public int getFrameSize() {
		return frameSize;
	}

	@Override
	public BufferedImage getSourceImage() {
		return sourceImage;
	}

	/**
	 * Creates a FramedTexture from any BufferedImage.
	 * If the image height equals its width, it's treated as a single-frame texture.
	 *
	 * @param image The source image.
	 * @return A new VerticalStripFramedTexture instance.
	 */
	public static VerticalStripFramedTexture fromImage(BufferedImage image) {
		return new VerticalStripFramedTexture(image);
	}
}
