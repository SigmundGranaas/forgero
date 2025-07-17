package com.sigmundgranaas.forgero.model.texture.impl;

import com.sigmundgranaas.forgero.model.texture.api.PalettizedTextureGenerator;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class AwtPalettizedTextureGenerator implements PalettizedTextureGenerator {

	private static final int PALETTE_COLOR_COUNT = 7; // The palette is 7 pixels wide.

	@Override
	public BufferedImage generate(BufferedImage template, BufferedImage palette) {


		Set<Integer> distinctTemplateGreys = new TreeSet<>();
		int width = template.getWidth();
		int height = template.getHeight();

		int paletteSize = palette.getWidth();
		// First pass: Collect all distinct greyscale values from the template
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int templatePixel = template.getRGB(x, y);
				int alpha = (templatePixel >> 24) & 0xff;

				if (alpha == 0) {
					continue; // Skip transparent pixels
				}

				int grey = templatePixel & 0xFF; // Get the greyscale value

				// Validate that R, G, B are approximately equal (to ensure it's truly greyscale)
				int r = (templatePixel >> 16) & 0xFF;
				int g = (templatePixel >> 8) & 0xFF;
				int b = templatePixel & 0xFF;
				if (Math.abs(r - grey) > 2 || Math.abs(g - grey) > 2 || Math.abs(b - grey) > 2) {
					throw new IllegalArgumentException(String.format(
							"Texture generation failed: Template pixel at (%d, %d) is not greyscale (RGB: %d,%d,%d). " +
									"All color channels must be approximately equal for greyscale mapping.",
							x, y, r, g, b
					));
				}
				distinctTemplateGreys.add(grey);
			}
		}

		// Validate distinct greyscale values against expected set
		List<Integer> sortedDistinctTemplateGreys = distinctTemplateGreys.stream().sorted().toList();

		if (sortedDistinctTemplateGreys.size() > paletteSize) {
			throw new IllegalArgumentException(String.format(
					"Texture generation failed: Template contains %d distinct greyscale values, but only %d are supported. " +
							"Distinct values found: %s",
					sortedDistinctTemplateGreys.size(),
					paletteSize,
					sortedDistinctTemplateGreys.stream().map(String::valueOf).collect(Collectors.joining(", "))
			));
		}

		// Second pass: Recolor the image
		BufferedImage coloredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int templatePixel = template.getRGB(x, y);
				int alpha = (templatePixel >> 24) & 0xff;

				if (alpha == 0) {
					continue; // Skip transparent pixels
				}

				int grey = templatePixel & 0xFF; // Get the greyscale value

				// Find the closest expected greyscale value for the current pixel's grey value
				// This is the "snapping" or "mapping" logic
				int mappedGreyIndex = findPaletteIndexForGrey(grey, sortedDistinctTemplateGreys, paletteSize);

				// Get the color from the palette using the determined index
				int paletteColor = palette.getRGB(mappedGreyIndex, 0);

				// Combine the palette color with the original alpha
				int finalColor = (alpha << 24) | (paletteColor & 0x00FFFFFF);
				coloredImage.setRGB(x, y, finalColor);
			}
		}
		return coloredImage;
	}

	/**
	 * Determines the correct 0-indexed palette column to use for a given greyscale value.
	 * This works by finding the given grey's relative position within the template's *actual*
	 * sorted distinct greyscale values, and mapping that to the 0-6 range of the palette.
	 *
	 * @param pixelGrey The greyscale value of the current pixel (0-255).
	 * @param distinctGreysInTemplate A sorted list of all distinct greyscale values found in the template.
	 * @return The 0-indexed column in the palette to pick a color from.
	 */
	private int findPaletteIndexForGrey(int pixelGrey, List<Integer> distinctGreysInTemplate, int paletteSize) {
		if (distinctGreysInTemplate.isEmpty()) {
			return 0; // Fallback, though validation should prevent this.
		}

		// Find the index of the pixel's grey value within the sorted list of distinct template greys.
		// If the exact value isn't found (due to floating point errors, etc.), find the closest.
		int templateValueIndex = Collections.binarySearch(distinctGreysInTemplate, pixelGrey);

		if (templateValueIndex < 0) {
			// If not found, binarySearch returns (-(insertion point) - 1).
			// We need to find the closest index.
			templateValueIndex = Math.min(-templateValueIndex - 1, distinctGreysInTemplate.size() - 1); // Closest upper bound index
			if (templateValueIndex > 0 && Math.abs(pixelGrey - distinctGreysInTemplate.get(templateValueIndex - 1)) < Math.abs(pixelGrey - distinctGreysInTemplate.get(templateValueIndex))) {
				templateValueIndex--; // Check if the lower bound is closer
			}
			templateValueIndex = Math.max(0, Math.min(templateValueIndex, distinctGreysInTemplate.size() - 1)); // Ensure bounds
		}


		// Now, map this index (0 to N-1, where N is distinctGreysInTemplate.size())
		// to the 0 to 6 range of the palette.
		float ratio = (float) templateValueIndex / (distinctGreysInTemplate.size() - 1);
		int paletteIndex = Math.round(ratio * (paletteSize - 1));

		return Math.max(0, Math.min(paletteIndex, paletteSize - 1));
	}
}
