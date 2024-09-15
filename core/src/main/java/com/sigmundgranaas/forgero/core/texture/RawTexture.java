package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Raw texture is a base class representing a texture generated from an InputStream.
 * This is used as the base class for the @Texture interface
 */
public record RawTexture(TextureIdentifier id,
                         BufferedImage image) implements Texture {

	public static RawTexture createRawTexture(TextureIdentifier id, InputStream stream) throws IOException {
		try (stream) {
			BufferedImage image = ImageIO.read(stream);
			return new RawTexture(id, image);
		}
	}

	@Override
	public InputStream getStream() throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(image, "png", os);
		InputStream is = new ByteArrayInputStream(os.toByteArray());
		os.close();
		return is;
	}

	@Override
	public BufferedImage getImage() {
		return image;
	}

	@Override
	public TextureIdentifier getId() {
		return id;
	}
}
