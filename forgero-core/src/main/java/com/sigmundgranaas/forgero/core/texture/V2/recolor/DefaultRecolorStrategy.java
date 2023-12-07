package com.sigmundgranaas.forgero.core.texture.V2.recolor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.texture.V2.Palette;
import com.sigmundgranaas.forgero.core.texture.V2.TemplateTexture;
import com.sigmundgranaas.forgero.core.texture.template.PixelInformation;
import com.sigmundgranaas.forgero.core.texture.utils.RgbColour;

public class DefaultRecolorStrategy implements RecolorStrategy {
	@Override
	public BufferedImage recolor(TemplateTexture template, Palette palette) {
		int templateFramesCount = template.getNumberOfGreyScales();
		int paletteFramesCount = palette.getNumberOfPalettes();

		// Case 1: Both have only one frame
		if (templateFramesCount == 1 && paletteFramesCount == 1) {
			return recolorSingleFrame(template, palette, 0);
		}

		// Determine the height of the final recolored image
		int recoloredImageHeight = template.getFrameSize() * Math.max(templateFramesCount, paletteFramesCount);
		BufferedImage recoloredImage = new BufferedImage(template.getFrameSize(), recoloredImageHeight, BufferedImage.TYPE_INT_ARGB);

		if (templateFramesCount > 1 && paletteFramesCount == 1) {
			// Case 2: Template has multiple frames, Palette has one
			for (int frameIndex = 0; frameIndex < templateFramesCount; frameIndex++) {
				BufferedImage frameImage = recolorSingleFrame(template, palette, frameIndex);
				copyFrameToImage(recoloredImage, frameImage, frameIndex, template.getFrameSize());
			}
		} else if (templateFramesCount == 1) {
			// Case 3: Template has one frame, Palette has multiple
			for (int frameIndex = 0; frameIndex < paletteFramesCount; frameIndex++) {
				BufferedImage frameImage = recolorSingleFrame(template, palette, frameIndex);
				copyFrameToImage(recoloredImage, frameImage, frameIndex, template.getFrameSize());
			}
		} else {
			// Case 4: Both have multiple frames
			for (int frameIndex = 0; frameIndex < templateFramesCount; frameIndex++) {
				int normalizedPaletteIndex = frameIndex % paletteFramesCount;
				BufferedImage frameImage = recolorSingleFrame(template, palette, normalizedPaletteIndex);
				copyFrameToImage(recoloredImage, frameImage, frameIndex, template.getFrameSize());
			}
		}

		return recoloredImage;
	}

	private BufferedImage recolorSingleFrame(TemplateTexture template, Palette palette, int frameIndex) {
		List<RgbColour> paletteValues = createUsableColourPalette(template, palette, frameIndex);
		List<RgbColour> greyScaleValues = template.getGreyScaleValues(frameIndex);
		List<PixelInformation> pixelValues = template.getPixelInfo(frameIndex);

		BufferedImage frameImage = new BufferedImage(template.getFrameSize(), template.getFrameSize(), BufferedImage.TYPE_INT_ARGB);
		for (PixelInformation pixel : pixelValues) {
			int colorIndex = findIntPosition(pixel.getRgbColor(), greyScaleValues);
			int rgb = paletteValues.get(colorIndex).getRgb();
			frameImage.setRGB(pixel.getLengthIndex(), pixel.getHeightIndex(), rgb);
		}
		return frameImage;
	}

	private void copyFrameToImage(BufferedImage targetImage, BufferedImage frame, int frameIndex, int frameSize) {
		int yPosition = frameIndex * frameSize;
		Graphics g = targetImage.createGraphics();
		g.drawImage(frame, 0, yPosition, null);
		g.dispose();
	}

	public int findIntPosition(RgbColour target, List<RgbColour> reference) {
		for (int i = 0; i < reference.size(); i++) {
			if (target.equals(reference.get(i))) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * Method for normalizing the colour palette to match the existing greyscale values of the template texture
	 *
	 * @return A colour palette matching the original greyscale values
	 */
	public List<RgbColour> createUsableColourPalette(TemplateTexture template, Palette palette, int frameIndex) {
		List<RgbColour> greyScaleValues = template.getGreyScaleValues(frameIndex);
		List<RgbColour> paletteValues = palette.getColourValues(frameIndex);
		int greyScaleSize = greyScaleValues.size();

		List<RgbColour> normalizedColourList = new ArrayList<>(greyScaleSize);

		if (greyScaleSize == paletteValues.size()) {
			normalizedColourList.addAll(paletteValues);
			return normalizedColourList;
		}

		for (int i = 0; i < greyScaleSize; i++) {
			float scaleValue = (float) paletteValues.size() / (float) greyScaleSize;
			float normalizedIndex = scaleValue * i;
			int newIndex = Math.min(Math.round(normalizedIndex), paletteValues.size() - 1);

			normalizedColourList.add(paletteValues.get(newIndex));
		}
		return normalizedColourList;
	}
}
