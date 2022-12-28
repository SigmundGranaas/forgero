package com.sigmundgranaas.forgero.texture.V2.recolor;

import com.sigmundgranaas.forgero.texture.utils.RgbColour;
import com.sigmundgranaas.forgero.texture.V2.Palette;
import com.sigmundgranaas.forgero.texture.V2.TemplateTexture;
import com.sigmundgranaas.forgero.texture.template.PixelInformation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DefaultRecolorStrategy implements RecolorStrategy {
    @Override
    public BufferedImage recolor(TemplateTexture template, Palette palette) {
        int paletteSize = palette.getColourValues().size();
        int greyScaleSize = template.getGreyScaleValues().size();
        List<RgbColour> paletteValues = createUsableColourPalette(template, palette);
        assert greyScaleSize >= 2 && paletteSize >= 2;

        BufferedImage recolouredImage = new BufferedImage(template.getImage().getWidth(), template.getImage().getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (PixelInformation pixel : template.getPixelValues()) {
            recolouredImage.setRGB(pixel.getLengthIndex(), pixel.getHeightIndex(), paletteValues.get(findIntPosition(pixel.getRgbColor(), template.getGreyScaleValues())).getRgb());
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
    public List<RgbColour> createUsableColourPalette(TemplateTexture template, Palette palette) {
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
