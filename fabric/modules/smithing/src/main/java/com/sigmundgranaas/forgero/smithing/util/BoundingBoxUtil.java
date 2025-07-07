package com.sigmundgranaas.forgero.smithing.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BoundingBoxUtil {
	private static final Logger LOGGER = LogManager.getLogger("ForgeroBoundingBoxUtil");

	public BoundingBox calculateBoundingBox(BufferedImage image) {
		if (image == null) {
			throw new IllegalArgumentException("Image cannot be null.");
		}

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		List<Point> validPixels = new ArrayList<>();

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

					// Save the pixel position
					validPixels.add(new Point(x, y));
				}
			}
		}

		// If no non-transparent pixels were found, reset min/max to indicate an empty box
		if (validPixels.isEmpty()) {
			minX = 0; // Conventionally, 0 or any other consistent value
			minY = 0;
			maxX = -1; // Max X < Min X indicates no width
			maxY = -1; // Max Y < Min Y indicates no height
		}

		LOGGER.info("[BoundingBoxUtil] Calculated bounding box: minX={}, minY={}, maxX={}, maxY={}, validPixels={}", minX, minY, maxX, maxY, validPixels.size());
		return new BoundingBox(minX, minY, maxX, maxY, validPixels);
	}

	public record BoundingBox(int minX, int minY, int maxX, int maxY, List<Point> validPixels) {

		public int getWidth() {
			if (validPixels.isEmpty()) {
				return 0;
			}
			return maxX - minX + 1;
		}


		public int getHeight() {
			if (validPixels.isEmpty()) {
				return 0;
			}
			return maxY - minY + 1;
		}

		public int getVisualCenterX() {
			if (validPixels.isEmpty()) {
				return 0;
			}
			// Improved: For even widths, center is (minX + maxX + 1) / 2
			return (minX + maxX + 1) / 2;
		}

		public int getVisualCenterY() {
			if (validPixels.isEmpty()) {
				return 0;
			}
			// Improved: For even heights, center is (minY + maxY + 1) / 2
			return (minY + maxY + 1) / 2;
		}

		public Point getCenteringOffset(int targetWidth, int targetHeight) {
			if (targetWidth <= 0 || targetHeight <= 0) {
				throw new IllegalArgumentException("Target width and height must be positive.");
			}

			if (validPixels.isEmpty()) {
				// If the image is entirely transparent, center the conceptual 'origin'
				// within the target area.
				return new Point(targetWidth / 2, targetHeight / 2);
			}

			// Calculate the target center relative to its own (0,0)
			int targetCenterX = targetWidth / 2;
			int targetCenterY = targetHeight / 2;

			// The offset needed is the difference between the target center and
			// the current visual center.
			int offsetX = targetCenterX - getVisualCenterX();
			int offsetY = targetCenterY - getVisualCenterY();

			Point offset = new Point(offsetX, offsetY);
			LOGGER.info("[BoundingBoxUtil] Centering offset for target {}x{}: {}", targetWidth, targetHeight, offset);
			return offset;
		}

		public Point getCenteringOffset16x16() {
			return getCenteringOffset(16, 16);
		}
	}

	// New static method to calculate item texture offset from image
	public static int[] getItemTextureOffsetFromImage(BufferedImage image) {
		BoundingBoxUtil util = new BoundingBoxUtil();
		BoundingBoxUtil.BoundingBox box = util.calculateBoundingBox(image);
		Point offset = box.getCenteringOffset16x16();
		LOGGER.info("[BoundingBoxUtil] getItemTextureOffsetFromImage: offset=({}, {})", offset.x, offset.y);
		return new int[] { offset.x, 0};
	}
}
