package com.sigmundgranaas.forgero.model.texture.impl;

import com.sigmundgranaas.forgero.model.texture.api.FramedTexture;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class VerticalStripFramedTextureTest {

	@Test
	void testSingleFrame() {
		// Create a 16x16 single-frame texture
		BufferedImage image = createTestImage(16, 16, Color.RED);

		FramedTexture texture = new VerticalStripFramedTexture(image);

		assertEquals(1, texture.getFrameCount());
		assertEquals(16, texture.getFrameSize());
		assertFalse(texture.isAnimated());
	}

	@Test
	void testMultipleFrames() {
		// Create a 16x48 texture (3 frames of 16x16)
		BufferedImage image = createMultiFrameImage(16, 3);

		FramedTexture texture = new VerticalStripFramedTexture(image);

		assertEquals(3, texture.getFrameCount());
		assertEquals(16, texture.getFrameSize());
		assertTrue(texture.isAnimated());
	}

	@Test
	void testFrameExtraction() {
		// Create a 16x32 texture with distinct colors per frame
		BufferedImage image = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		// Frame 0: Red
		g.setColor(Color.RED);
		g.fillRect(0, 0, 16, 16);

		// Frame 1: Blue
		g.setColor(Color.BLUE);
		g.fillRect(0, 16, 16, 16);

		g.dispose();

		FramedTexture texture = new VerticalStripFramedTexture(image);

		assertEquals(2, texture.getFrameCount());

		// Verify frame 0 is red
		BufferedImage frame0 = texture.getFrame(0);
		assertEquals(16, frame0.getWidth());
		assertEquals(16, frame0.getHeight());
		assertEquals(Color.RED.getRGB(), frame0.getRGB(8, 8));

		// Verify frame 1 is blue
		BufferedImage frame1 = texture.getFrame(1);
		assertEquals(Color.BLUE.getRGB(), frame1.getRGB(8, 8));
	}

	@Test
	void testGetSourceImage() {
		BufferedImage image = createTestImage(16, 16, Color.GREEN);

		FramedTexture texture = new VerticalStripFramedTexture(image);

		assertSame(image, texture.getSourceImage());
	}

	@Test
	void testFrameIndexOutOfBounds() {
		BufferedImage image = createTestImage(16, 32, Color.RED);
		FramedTexture texture = new VerticalStripFramedTexture(image);

		assertThrows(IndexOutOfBoundsException.class, () -> texture.getFrame(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> texture.getFrame(2));
	}

	@Test
	void testInvalidDimensions() {
		// Height not a multiple of width
		BufferedImage image = createTestImage(16, 17, Color.RED);

		assertThrows(IllegalArgumentException.class, () -> new VerticalStripFramedTexture(image));
	}

	@Test
	void testNullImage() {
		assertThrows(IllegalArgumentException.class, () -> new VerticalStripFramedTexture(null));
	}

	@Test
	void testStaticFactoryMethod() {
		BufferedImage image = createTestImage(16, 16, Color.YELLOW);

		FramedTexture texture = VerticalStripFramedTexture.fromImage(image);

		assertNotNull(texture);
		assertEquals(1, texture.getFrameCount());
	}

	/**
	 * Creates a solid-color test image.
	 */
	private BufferedImage createTestImage(int width, int height, Color color) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setColor(color);
		g.fillRect(0, 0, width, height);
		g.dispose();
		return image;
	}

	/**
	 * Creates a multi-frame test image with distinct colors per frame.
	 */
	private BufferedImage createMultiFrameImage(int frameSize, int frameCount) {
		Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA};
		BufferedImage image = new BufferedImage(frameSize, frameSize * frameCount, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		for (int i = 0; i < frameCount; i++) {
			g.setColor(colors[i % colors.length]);
			g.fillRect(0, i * frameSize, frameSize, frameSize);
		}
		g.dispose();
		return image;
	}
}
