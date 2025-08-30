package com.sigmundgranaas.forgero.smithing.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BoundingBoxUtil {
	private static final Logger LOGGER = LogManager.getLogger("ForgeroBoundingBoxUtil");

	/**
	 * Calculates the bounding box of non-transparent pixels in the image.
	 * @param image the image to analyze
	 * @return BoundingBox, or null if the image is fully transparent
	 */
	public BoundingBox calculateBoundingBox(BufferedImage image) {
		if (image == null) {
			throw new IllegalArgumentException("Image cannot be null.");
		}

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		boolean found = false;

		int width = image.getWidth();
		int height = image.getHeight();

		// Iterate through each pixel of the image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = image.getRGB(x, y); // Get the RGB value, which includes alpha
				int alpha = (pixel >> 24) & 0xFF; // Extract the alpha component (0-255)

				// If alpha is greater than 0, the pixel is not fully transparent
				if (alpha > 0) {
					// Update min/max coordinates
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
					found = true;
				}
			}
		}

		// Early exit if no non-transparent pixels were found
		if (!found) {
			return null;
		}

		return new BoundingBox(minX, minY, maxX, maxY);
	}

	/**
	 * Collects all non-transparent pixel positions in the image.
	 * @param image the image to analyze
	 * @return List of Points for all non-transparent pixels
	 */
	public List<Point> collectValidPixels(BufferedImage image) {
		List<Point> validPixels = new ArrayList<>();
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = image.getRGB(x, y);
				int alpha = (pixel >> 24) & 0xFF;
				if (alpha > 0) {
					validPixels.add(new Point(x, y));
				}
			}
		}
		return validPixels;
	}

	public record BoundingBox(int minX, int minY, int maxX, int maxY) {
		public int getWidth() {
			if (maxX < minX) {
				return 0;
			}
			return maxX - minX + 1;
		}

		public int getHeight() {
			if (maxY < minY) {
				return 0;
			}
			return maxY - minY + 1;
		}

		public int getVisualCenterX() {
			if (maxX < minX) {
				return 0;
			}
			return (minX + maxX + 1) / 2;
		}

		public int getVisualCenterY() {
			if (maxY < minY) {
				return 0;
			}
			return (minY + maxY + 1) / 2;
		}

		public Point getCenteringOffset(int targetWidth, int targetHeight) {
			if (targetWidth <= 0 || targetHeight <= 0) {
				throw new IllegalArgumentException("Target width and height must be positive.");
			}
			if (maxX < minX || maxY < minY) {
				// If the image is entirely transparent, center the conceptual 'origin'
				// within the target area.
				return new Point(targetWidth / 2, targetHeight / 2);
			}
			int targetCenterX = targetWidth / 2;
			int targetCenterY = targetHeight / 2;
			int offsetX = targetCenterX - getVisualCenterX();
			int offsetY = targetCenterY - getVisualCenterY();
			return new Point(offsetX, offsetY);
		}

		public Point getCenteringOffset16x16() {
			return getCenteringOffset(16, 16);
		}
	}

	// New static method to calculate item texture offset from image
	public static int[] getItemTextureOffsetFromImage(BufferedImage image) {
		BoundingBoxUtil util = new BoundingBoxUtil();
		BoundingBoxUtil.BoundingBox box = util.calculateBoundingBox(image);
		if (box == null) {
			return new int[] { 8, 8 };
		}
		Point offset = box.getCenteringOffset16x16();
		return new int[] { offset.x, offset.y };
	}
}
