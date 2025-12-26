package com.sigmundgranaas.forgero.model.texture.impl;

import com.sigmundgranaas.forgero.model.texture.api.FramedPalette;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class RowBasedFramedPaletteTest {

	@Test
	void testSingleRowPalette() {
		// Create a 7x1 single-row palette
		BufferedImage image = createPaletteImage(7, 1);

		FramedPalette palette = new RowBasedFramedPalette(image);

		assertEquals(1, palette.getFrameCount());
		assertEquals(7, palette.getPaletteWidth());
		assertFalse(palette.isAnimated());
	}

	@Test
	void testMultiRowPalette() {
		// Create a 7x3 palette (3 frames)
		BufferedImage image = createPaletteImage(7, 3);

		FramedPalette palette = new RowBasedFramedPalette(image);

		assertEquals(3, palette.getFrameCount());
		assertEquals(7, palette.getPaletteWidth());
		assertTrue(palette.isAnimated());
	}

	@Test
	void testRowExtraction() {
		// Create a palette with distinct rows
		BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);

		// Row 0: Red, Green, Blue
		image.setRGB(0, 0, Color.RED.getRGB());
		image.setRGB(1, 0, Color.GREEN.getRGB());
		image.setRGB(2, 0, Color.BLUE.getRGB());

		// Row 1: Yellow, Cyan, Magenta
		image.setRGB(0, 1, Color.YELLOW.getRGB());
		image.setRGB(1, 1, Color.CYAN.getRGB());
		image.setRGB(2, 1, Color.MAGENTA.getRGB());

		FramedPalette palette = new RowBasedFramedPalette(image);

		assertEquals(2, palette.getFrameCount());

		// Verify row 0
		BufferedImage row0 = palette.getPaletteRow(0);
		assertEquals(3, row0.getWidth());
		assertEquals(1, row0.getHeight());
		assertEquals(Color.RED.getRGB(), row0.getRGB(0, 0));
		assertEquals(Color.GREEN.getRGB(), row0.getRGB(1, 0));
		assertEquals(Color.BLUE.getRGB(), row0.getRGB(2, 0));

		// Verify row 1
		BufferedImage row1 = palette.getPaletteRow(1);
		assertEquals(Color.YELLOW.getRGB(), row1.getRGB(0, 0));
		assertEquals(Color.CYAN.getRGB(), row1.getRGB(1, 0));
		assertEquals(Color.MAGENTA.getRGB(), row1.getRGB(2, 0));
	}

	@Test
	void testGetSourceImage() {
		BufferedImage image = createPaletteImage(7, 1);

		FramedPalette palette = new RowBasedFramedPalette(image);

		assertSame(image, palette.getSourceImage());
	}

	@Test
	void testRowIndexOutOfBounds() {
		BufferedImage image = createPaletteImage(7, 2);
		FramedPalette palette = new RowBasedFramedPalette(image);

		assertThrows(IndexOutOfBoundsException.class, () -> palette.getPaletteRow(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> palette.getPaletteRow(2));
	}

	@Test
	void testNullImage() {
		assertThrows(IllegalArgumentException.class, () -> new RowBasedFramedPalette(null));
	}

	@Test
	void testStaticFactoryMethod() {
		BufferedImage image = createPaletteImage(7, 1);

		FramedPalette palette = RowBasedFramedPalette.fromImage(image);

		assertNotNull(palette);
		assertEquals(1, palette.getFrameCount());
	}

	/**
	 * Creates a test palette image with gradient colors per row.
	 */
	private BufferedImage createPaletteImage(int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// Create a gradient from dark to light
				int brightness = (int) (255 * ((float) x / (width - 1)));
				Color color = new Color(brightness, brightness / 2, brightness / 3);
				image.setRGB(x, y, color.getRGB());
			}
		}
		g.dispose();
		return image;
	}
}
