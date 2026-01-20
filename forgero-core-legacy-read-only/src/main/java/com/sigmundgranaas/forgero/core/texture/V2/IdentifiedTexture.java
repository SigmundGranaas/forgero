package com.sigmundgranaas.forgero.core.texture.V2;

import com.sigmundgranaas.forgero.core.state.Identifiable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("ClassCanBeRecord")
public class IdentifiedTexture implements Identifiable, Texture {
	private final String name;
	private final String nameSpace;
	private final BufferedImage image;

	public IdentifiedTexture(String name, String nameSpace, BufferedImage image) {
		this.name = name;
		this.nameSpace = nameSpace;
		this.image = image;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String nameSpace() {
		return nameSpace;
	}

	@Override
	public InputStream getStream() throws IOException {
		return Texture.imageToStream(image);
	}

	@Override
	public BufferedImage getImage() {
		return image;
	}
}
