package com.sigmundgranaas.forgero.model.texture.impl;

import com.sigmundgranaas.forgero.model.texture.api.FramedPalette;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A framed palette implementation where each row represents a single animation frame.
 * Row 0 is frame 0, row 1 is frame 1, etc.
 * <p>
 * For example, a 7x3 palette image contains 3 frames with 7 colors each.
 */
public class RowBasedFramedPalette implements FramedPalette {
	private final BufferedImage sourceImage;
	private final int paletteWidth;
	private final List<BufferedImage> paletteRows;

	/**
	 * Creates a framed palette from a source image.
	 * Each row becomes one frame of the palette.
	 *
	 * @param sourceImage The source palette image.
	 * @throws IllegalArgumentException if the image is null or has invalid dimensions.
	 */
	public RowBasedFramedPalette(BufferedImage sourceImage) {
		if (sourceImage == null) {
			throw new IllegalArgumentException("Source image cannot be null");
		}

		if (sourceImage.getWidth() <= 0 || sourceImage.getHeight() <= 0) {
			throw new IllegalArgumentException("Image dimensions must be positive");
		}

		this.sourceImage = sourceImage;
		this.paletteWidth = sourceImage.getWidth();
		this.paletteRows = new ArrayList<>();

		extractRows();
	}

	private void extractRows() {
		int rowCount = sourceImage.getHeight();
		for (int y = 0; y < rowCount; y++) {
			BufferedImage row = sourceImage.getSubimage(0, y, paletteWidth, 1);
			// Create a copy to avoid subimage reference issues
			BufferedImage rowCopy = new BufferedImage(paletteWidth, 1, BufferedImage.TYPE_INT_ARGB);
			rowCopy.getGraphics().drawImage(row, 0, 0, null);
			paletteRows.add(rowCopy);
		}
	}

	@Override
	public BufferedImage getPaletteRow(int index) {
		if (index < 0 || index >= paletteRows.size()) {
			throw new IndexOutOfBoundsException(String.format(
					"Palette frame index %d out of bounds [0, %d)", index, paletteRows.size()
			));
		}
		return paletteRows.get(index);
	}

	@Override
	public int getFrameCount() {
		return paletteRows.size();
	}

	@Override
	public int getPaletteWidth() {
		return paletteWidth;
	}

	@Override
	public BufferedImage getSourceImage() {
		return sourceImage;
	}

	/**
	 * Creates a FramedPalette from any BufferedImage.
	 * Single-row images are treated as single-frame palettes.
	 *
	 * @param image The source palette image.
	 * @return A new RowBasedFramedPalette instance.
	 */
	public static RowBasedFramedPalette fromImage(BufferedImage image) {
		return new RowBasedFramedPalette(image);
	}
}
