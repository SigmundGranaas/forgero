package com.sigmundgranaas.forgero.smithing.util;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class BoundingBoxUtil {
	private BoundingBoxUtil() {
	}

	public static BoundingBox calculateBoundingBox(BufferedImage image) {
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

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int alpha = (image.getRGB(x, y) >> 24) & 0xFF;

				if (alpha > 0) {
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
					found = true;
				}
			}
		}

		if (!found) {
			return null;
		}

		return new BoundingBox(minX, minY, maxX, maxY);
	}

	public static List<Point> collectValidPixels(BufferedImage image) {
		List<Point> validPixels = new ArrayList<>();
		int width = image.getWidth();
		int height = image.getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int alpha = (image.getRGB(x, y) >> 24) & 0xFF;
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

	public static int[] getItemTextureOffsetFromImage(BufferedImage image) {
		BoundingBox box = calculateBoundingBox(image);
		if (box == null) {
			return new int[] { 8, 8 };
		}
		Point offset = box.getCenteringOffset16x16();
		return new int[] { offset.x, offset.y };
	}
}
