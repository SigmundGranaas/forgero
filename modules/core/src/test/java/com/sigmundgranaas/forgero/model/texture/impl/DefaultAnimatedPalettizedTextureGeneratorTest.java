package com.sigmundgranaas.forgero.model.texture.impl;

import com.sigmundgranaas.forgero.model.texture.api.*;
import com.sigmundgranaas.forgero.model.texture.dto.AnimationMetadataDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DefaultAnimatedPalettizedTextureGeneratorTest {

	private AnimatedPalettizedTextureGenerator animatedGenerator;

	@BeforeEach
	void setUp() {
		// Use a simple mock palette generator that just copies template colors
		// In real usage, this would be AwtPalettizedTextureGenerator
		PalettizedTextureGenerator simpleGenerator = (template, palette) -> {
			// Simple implementation: just return a copy of the template
			BufferedImage result = new BufferedImage(
					template.getWidth(),
					template.getHeight(),
					BufferedImage.TYPE_INT_ARGB
			);
			result.getGraphics().drawImage(template, 0, 0, null);
			return result;
		};

		animatedGenerator = new DefaultAnimatedPalettizedTextureGenerator(simpleGenerator);
	}

	@Test
	void testStaticTemplateStaticPalette() {
		// Scenario 1: Both single frame → static output
		FramedTexture template = createFramedTexture(16, 1);
		FramedPalette palette = createFramedPalette(7, 1);

		TextureGenerationResult result = animatedGenerator.generate(
				template, palette,
				Optional.empty(), Optional.empty()
		);

		assertFalse(result.isAnimated());
		assertEquals(16, result.image().getWidth());
		assertEquals(16, result.image().getHeight()); // Single frame
	}

	@Test
	void testAnimatedTemplateStaticPalette() {
		// Scenario 2: Animated template + static palette → animated output
		FramedTexture template = createFramedTexture(16, 3); // 3 frames
		FramedPalette palette = createFramedPalette(7, 1);   // 1 frame

		AnimationMetadataDTO templateMeta = new AnimationMetadataDTO(2, false, null);

		TextureGenerationResult result = animatedGenerator.generate(
				template, palette,
				Optional.of(templateMeta), Optional.empty()
		);

		assertTrue(result.isAnimated());
		assertEquals(16, result.image().getWidth());
		assertEquals(16 * 3, result.image().getHeight()); // 3 frames stacked
		assertEquals(2, result.metadata().get().frametime());
	}

	@Test
	void testStaticTemplateAnimatedPalette() {
		// Scenario 3: Static template + animated palette → animated output
		FramedTexture template = createFramedTexture(16, 1); // 1 frame
		FramedPalette palette = createFramedPalette(7, 4);   // 4 frames

		AnimationMetadataDTO paletteMeta = new AnimationMetadataDTO(3, true, null);

		TextureGenerationResult result = animatedGenerator.generate(
				template, palette,
				Optional.empty(), Optional.of(paletteMeta)
		);

		assertTrue(result.isAnimated());
		assertEquals(16, result.image().getWidth());
		assertEquals(16 * 4, result.image().getHeight()); // 4 frames stacked
		// Palette metadata used since template has none
		assertEquals(3, result.metadata().get().frametime());
		assertTrue(result.metadata().get().interpolate());
	}

	@Test
	void testAnimatedTemplateAnimatedPalette() {
		// Scenario 4: Both animated → animated output (template takes precedence)
		FramedTexture template = createFramedTexture(16, 3); // 3 frames
		FramedPalette palette = createFramedPalette(7, 2);   // 2 frames (will cycle)

		AnimationMetadataDTO templateMeta = new AnimationMetadataDTO(5, false, null);
		AnimationMetadataDTO paletteMeta = new AnimationMetadataDTO(1, true, List.of(0, 1));

		TextureGenerationResult result = animatedGenerator.generate(
				template, palette,
				Optional.of(templateMeta), Optional.of(paletteMeta)
		);

		assertTrue(result.isAnimated());
		assertEquals(16, result.image().getWidth());
		assertEquals(16 * 3, result.image().getHeight()); // Uses template frame count
		// Template metadata takes precedence
		assertEquals(5, result.metadata().get().frametime());
		assertFalse(result.metadata().get().interpolate());
	}

	@Test
	void testDefaultMetadataWhenNoneProvided() {
		// Multi-frame template without explicit metadata
		FramedTexture template = createFramedTexture(16, 2);
		FramedPalette palette = createFramedPalette(7, 1);

		TextureGenerationResult result = animatedGenerator.generate(
				template, palette,
				Optional.empty(), Optional.empty()
		);

		// Should still detect animation and use default metadata
		assertTrue(result.isAnimated());
		assertEquals(1, result.metadata().get().frametime()); // Default
		assertFalse(result.metadata().get().interpolate()); // Default
	}

	/**
	 * Creates a test framed texture with the specified frame count.
	 */
	private FramedTexture createFramedTexture(int frameSize, int frameCount) {
		BufferedImage image = new BufferedImage(
				frameSize,
				frameSize * frameCount,
				BufferedImage.TYPE_INT_ARGB
		);
		Graphics2D g = image.createGraphics();

		Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};
		for (int i = 0; i < frameCount; i++) {
			g.setColor(colors[i % colors.length]);
			g.fillRect(0, i * frameSize, frameSize, frameSize);
		}
		g.dispose();

		return new VerticalStripFramedTexture(image);
	}

	/**
	 * Creates a test framed palette with the specified row count.
	 */
	private FramedPalette createFramedPalette(int width, int rowCount) {
		BufferedImage image = new BufferedImage(width, rowCount, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		for (int y = 0; y < rowCount; y++) {
			for (int x = 0; x < width; x++) {
				int brightness = (int) (255 * ((float) x / (width - 1)));
				image.setRGB(x, y, new Color(brightness, brightness, brightness).getRGB());
			}
		}
		g.dispose();

		return new RowBasedFramedPalette(image);
	}
}
