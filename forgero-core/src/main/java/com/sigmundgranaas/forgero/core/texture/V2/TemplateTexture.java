package com.sigmundgranaas.forgero.core.texture.V2;

import com.sigmundgranaas.forgero.core.texture.V2.recolor.RecolorStrategy;
import com.sigmundgranaas.forgero.core.texture.template.PixelInformation;
import com.sigmundgranaas.forgero.core.texture.utils.RgbColour;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateTexture implements Texture {
	private final BufferedImage template;
	private final RecolorStrategy strategy;
	private List<PixelInformation> pixelValues;
	private List<RgbColour> greyScaleValues;

	public TemplateTexture(BufferedImage template, RecolorStrategy strategy) {
		this.template = template;
		this.strategy = strategy;
		setTemplateValues();
	}

	public List<PixelInformation> getPixelValues() {
		return pixelValues;
	}

	public List<RgbColour> getGreyScaleValues() {
		return greyScaleValues;
	}

	@Override
	public InputStream getStream() throws IOException {
		return Texture.imageToStream(getImage());
	}

	@Override
	public BufferedImage getImage() {
		return template;
	}

	private void setTemplateValues() {
		ArrayList<PixelInformation> pixelValues = new ArrayList<>();
		HashSet<RgbColour> greyScaleValueSet = new HashSet<>();

		for (int y = 0; y < template.getHeight(); ++y) {
			for (int x = 0; x < template.getWidth(); ++x) {
				if (template.getRGB(x, y) != 0) {
					greyScaleValueSet.add(new RgbColour(template.getRGB(x, y)));
					pixelValues.add(new PixelInformation(x, y, new RgbColour(template.getRGB(x, y))));
				}
			}
		}

		this.greyScaleValues = greyScaleValueSet.stream().sorted().collect(Collectors.toList());
		this.pixelValues = pixelValues;
	}

	public RawTexture apply(Palette palette) {
		return new RawTexture(strategy.recolor(this, palette));
	}
}
