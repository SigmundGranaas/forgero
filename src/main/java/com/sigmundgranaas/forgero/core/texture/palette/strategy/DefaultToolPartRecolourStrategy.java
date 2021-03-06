package com.sigmundgranaas.forgero.core.texture.palette.strategy;

import com.sigmundgranaas.forgero.core.texture.palette.Palette;
import com.sigmundgranaas.forgero.core.texture.palette.RecolourStrategy;
import com.sigmundgranaas.forgero.core.texture.template.PixelInformation;
import com.sigmundgranaas.forgero.core.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.core.texture.utils.RgbColour;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Default recolour strategy. This strategy won't ensure both dark shades wil be chosen for creating the outline of the tool.
 */
public class DefaultToolPartRecolourStrategy implements RecolourStrategy {
    protected final TemplateTexture template;
    protected final Palette palette;

    public DefaultToolPartRecolourStrategy(TemplateTexture template, Palette palette) {
        this.template = template;
        this.palette = palette;
    }

    @Override
    public BufferedImage recolour() {
        return createRecolouredImage();
    }


    public BufferedImage createRecolouredImage() {
        int paletteSize = palette.getColourValues().size();
        int greyScaleSize = template.getGreyScaleValues().size();
        List<RgbColour> palette = createUsableColourPalette();
        assert greyScaleSize >= 2 && paletteSize >= 2;

        BufferedImage recolouredImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (PixelInformation pixel : template.getPixelValues()) {
            recolouredImage.setRGB(pixel.getLengthIndex(), pixel.getHeightIndex(), palette.get(findIntPosition(pixel.getRgbColor(), template.getGreyScaleValues())).getRgb());
        }
        return recolouredImage;
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
    public List<RgbColour> createUsableColourPalette() {
        int greyScaleSize = template.getGreyScaleValues().size();

        List<RgbColour> colourList = new ArrayList<>(greyScaleSize);
        if (greyScaleSize == palette.getColourValues().size()) {
            for (int i = 0; i < greyScaleSize; i++) {
                colourList.add(palette.getColourValues().get(i));
            }
            return colourList;
        }

        for (int i = 0; i < greyScaleSize; i++) {
            float scaleValue = (float) palette.getColourValues().size() / (float) greyScaleSize;
            float normalized = scaleValue * i;
            int newIndex = Math.round(normalized);

            if (newIndex == palette.getColourValues().size()) {
                newIndex--;
            }

            if (newIndex == 0 || i == 0) {
                colourList.add(0, palette.getColourValues().get(0));
            } else {
                colourList.add(i, palette.getColourValues().get(newIndex));
            }
        }
        return colourList;
    }

}
