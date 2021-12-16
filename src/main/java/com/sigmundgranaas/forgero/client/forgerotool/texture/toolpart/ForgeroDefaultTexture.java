package com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.PixelInformation;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.client.forgerotool.texture.utils.RgbColour;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ForgeroDefaultTexture extends AbstractForgeroToolPartTexture {
    public ForgeroDefaultTexture(TemplateTexture template, MaterialPalette palette) {
        super(template, palette);
    }

    public BufferedImage createRecolouredImage() {
        int paletteSize = palette.getColourValues().size();
        int greyScaleSize = template.getGreyScaleValues().size();
        List<RgbColour> palette = createUsableColourPalette();
        assert greyScaleSize >= 2 && paletteSize >= 2;

        BufferedImage recolouredImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (PixelInformation pixel : template.getPixelValues()) {
            recolouredImage.setRGB(pixel.heightIndex(), pixel.getLengthIndex(), palette.get(findIntPosition(pixel.getRgbColor(), template.getGreyScaleValues())).getRgb());
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
    private List<RgbColour> createUsableColourPalette() {
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

            if (newIndex == 0 || i == 0) {
                colourList.add(0, palette.getColourValues().get(0));
            } else {
                colourList.add(i, palette.getColourValues().get(newIndex));
            }
        }
        return colourList;
    }

    @Override
    public @NotNull BufferedImage performRecolour() {
        return createRecolouredImage();
    }
}
