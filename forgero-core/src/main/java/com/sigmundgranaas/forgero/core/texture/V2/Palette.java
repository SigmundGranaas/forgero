package com.sigmundgranaas.forgero.core.texture.V2;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.texture.utils.RgbColour;

public class Palette implements Texture {
	private final BufferedImage palette;
	private final List<FramePalette> framePalettes;

	public Palette(BufferedImage palette) {
		this.palette = palette;
		this.framePalettes = new ArrayList<>();
		extractPalettes();
	}

	private void extractPalettes() {
		int frameHeight = palette.getHeight();
		for (int y = 0; y < frameHeight; y++) {
			List<RgbColour> colors = new ArrayList<>();
			for (int x = 0; x < palette.getWidth(); x++) {
				int rgb = palette.getRGB(x, y);
				if ((rgb & 0xFF000000) != 0x00000000) {
					colors.add(new RgbColour(rgb));
				}
			}
			FramePalette framePalette = new FramePalette(y, colors);
			framePalettes.add(framePalette);
		}
	}

	@Override
	public InputStream getStream() throws IOException {
		return Texture.imageToStream(palette);
	}

	@Override
	public BufferedImage getImage() {
		return palette;
	}

	public List<RgbColour> getColourValues(int frameIndex) {
		if (frameIndex >= 0 && frameIndex < framePalettes.size()) {
			return framePalettes.get(frameIndex).colours();
		} else {
			return framePalettes.get(0).colours();
		}
	}

	public int getNumberOfPalettes() {
		return framePalettes.size();
	}

	public record FramePalette(int frameIndex, List<RgbColour> colours) {
	}
}
