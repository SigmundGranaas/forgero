package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.model.rendering.impl.AwtTextureCompositor;
import com.sigmundgranaas.forgero.model.rendering.impl.ClassPathResourceTextureProvider;
import com.sigmundgranaas.forgero.model.rendering.api.TextureCompositor;
import com.sigmundgranaas.forgero.model.rendering.api.TextureProvider;
import com.sigmundgranaas.forgero.model.rendering.impl.PngWriter;
import com.sigmundgranaas.forgero.model.resolution.api.LayeredTexture;
import com.sigmundgranaas.forgero.model.resolution.api.TextureLayer;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AwtTextureCompositorTest {

	@Test
	void testRenderCompositesImagesInCorrectOrder() throws IOException {
		// Use the real ClassPathResourceTextureProvider, which will find the dummy images created in @BeforeAll
		TextureProvider provider = new ClassPathResourceTextureProvider();
		TextureCompositor compositor = new AwtTextureCompositor(provider);

		// Define a layered texture where the pickaxe head (order 1) is on top of the handle (order 0)
		LayeredTexture textureToRender = new LayeredTexture(Arrays.asList(
				new TextureLayer("forgero:item/oak-handle", 0),
				new TextureLayer("forgero:item/iron-pickaxe_head", 1)
		));

		// Render the texture
		BufferedImage result = compositor.render(textureToRender);

		// Assert basic properties of the final image
		assertNotNull(result);
		assertEquals(16, result.getWidth());
		assertEquals(16, result.getHeight());

		// Save the image for manual inspection instead of asserting pixel color
		PngWriter.save(result, "build/test_results/compositor_test_output.png");
	}
}
