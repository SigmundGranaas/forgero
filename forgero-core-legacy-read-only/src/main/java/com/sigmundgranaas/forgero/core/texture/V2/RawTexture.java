package com.sigmundgranaas.forgero.core.texture.V2;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public record RawTexture(BufferedImage image) implements Texture {

	@Override
	public InputStream getStream() throws IOException {
		return Texture.imageToStream(image);
	}

	@Override
	public BufferedImage getImage() {
		return image;
	}

	public IdentifiedTexture of(String name, String nameSpace) {
		return new IdentifiedTexture(name, nameSpace, image);
	}
}
