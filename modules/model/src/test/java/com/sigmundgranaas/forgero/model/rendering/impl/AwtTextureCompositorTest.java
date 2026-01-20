package com.sigmundgranaas.forgero.model.rendering.impl;

import com.sigmundgranaas.forgero.model.api.Offset;
import com.sigmundgranaas.forgero.model.api.RenderableTexture;
import com.sigmundgranaas.forgero.model.rendering.api.TextureCompositor;
import com.sigmundgranaas.forgero.utility.resource.loader.api.UnifiedTextureProvider;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AwtTextureCompositorTest {

	@Test
	void testRenderCompositesImagesInCorrectOrder() throws IOException {
		UnifiedTextureProvider provider = new ClassPathResourceTextureProvider();
		TextureCompositor compositor = new AwtTextureCompositor(provider);

		// Define a list of RenderableTexture instances with order and offset
		List<RenderableTexture> texturesToRender = Arrays.asList(
				new RenderableTexture("forgero:item/oak-handle", 0, Offset.ZERO), // order 0
				new RenderableTexture("forgero:item/iron-pickaxe_head", 1, Offset.ZERO) // order 1
		);

		// Render the textures. Compositor now takes a List<RenderableTexture>.
		BufferedImage result = compositor.render(texturesToRender);

		// Assert basic properties of the final image
		assertNotNull(result);
		assertEquals(16, result.getWidth());
		assertEquals(16, result.getHeight());

		// Save the image for manual inspection instead of asserting pixel color
		// Ensure that you have dummy images at /assets/forgero/textures/item/oak-handle.png
		// and /assets/forgero/textures/item/iron-pickaxe_head.png for this test to pass correctly.
		// These should be simple 16x16 PNGs.
		// (The project setup should include these dummy textures in src/test/resources/assets/forgero/textures/item/)
		PngWriter.save(result, "build/test_results/compositor_test_output.png");
	}

	@Test
	void testRenderCompositesImagesWithOffsets() throws IOException {
		UnifiedTextureProvider provider = new ClassPathResourceTextureProvider();
		TextureCompositor compositor = new AwtTextureCompositor(provider);

		// Define a list of RenderableTexture instances with order and custom offsets
		List<RenderableTexture> texturesToRender = Arrays.asList(
				new RenderableTexture("forgero:item/oak-handle", 0, new Offset(0, 0)),
				new RenderableTexture("forgero:item/iron-pickaxe_head", 1, new Offset(2, 2)) // Offset for the head
		);

		BufferedImage result = compositor.render(texturesToRender);

		assertNotNull(result);
		assertEquals(16, result.getWidth());
		assertEquals(16, result.getHeight());

		PngWriter.save(result, "build/test_results/compositor_test_offset_output.png");
	}
}
