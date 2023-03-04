package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for identifying textures that can be handled by the Texture service.
 * <p>
 * All palettes, templates and generated textures implement this interface.
 * This interface is used to generate textures from templates and materials.
 */
public interface Texture {
	InputStream getStream() throws IOException;

	TextureIdentifier getId();

	BufferedImage getImage();
}
