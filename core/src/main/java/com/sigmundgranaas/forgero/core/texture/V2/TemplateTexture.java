package com.sigmundgranaas.forgero.core.texture.V2;

import com.sigmundgranaas.forgero.core.texture.V2.recolor.RecolorStrategy;
import com.sigmundgranaas.forgero.core.texture.template.PixelInformation;
import com.sigmundgranaas.forgero.core.texture.utils.RgbColour;
import lombok.Getter;

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
	@Getter
	private final int frameSize;
	private final List<List<PixelInformation>> framesPixelInfo;
	private final List<List<RgbColour>> framesGreyScaleValues;

	public TemplateTexture(BufferedImage template, RecolorStrategy strategy) {
		this.template = template;
		this.strategy = strategy;
		this.frameSize = template.getWidth();
		this.framesPixelInfo = new ArrayList<>();
		this.framesGreyScaleValues = new ArrayList<>();
		processFrames();
	}

	private void processFrames() {
		int numFrames = template.getHeight() / frameSize;
		for (int i = 0; i < numFrames; i++) {
			BufferedImage frame = template.getSubimage(0, i * frameSize, frameSize, frameSize);
			framesPixelInfo.add(extractPixelInfo(frame));
		}
	}

	private List<PixelInformation> extractPixelInfo(BufferedImage frame) {
		List<PixelInformation> pixelInfoList = new ArrayList<>();
		HashSet<RgbColour> greyScaleSet = new HashSet<>();

		for (int y = 0; y < frame.getHeight(); ++y) {
			for (int x = 0; x < frame.getWidth(); ++x) {
				int rgb = frame.getRGB(x, y);
				if ((rgb & 0xFF000000) != 0x00000000) {
					RgbColour colour = new RgbColour(rgb);
					pixelInfoList.add(new PixelInformation(x, y, colour));
					greyScaleSet.add(colour);
				}
			}
		}

		framesGreyScaleValues.add(greyScaleSet.stream().sorted().collect(Collectors.toList()));
		return pixelInfoList;
	}

	@Override
	public InputStream getStream() throws IOException {
		return Texture.imageToStream(getImage());
	}


	@Override
	public BufferedImage getImage() {
		return template;
	}


	public List<RgbColour> getGreyScaleValues(int frameIndex) {
		if (frameIndex >= 0 && frameIndex < framesGreyScaleValues.size()) {
			return framesGreyScaleValues.get(frameIndex);
		} else {
			return framesGreyScaleValues.get(0);
		}
	}

	public List<PixelInformation> getPixelInfo(int frameIndex) {
		if (frameIndex >= 0 && frameIndex < framesPixelInfo.size()) {
			return framesPixelInfo.get(frameIndex);
		} else {
			return framesPixelInfo.get(0);
		}
	}

	public int getNumberOfGreyScales() {
		return framesGreyScaleValues.size();
	}

	public RawTexture apply(Palette palette) {
		return new RawTexture(strategy.recolor(this, palette));
	}
}
