package com.sigmundgranaas.forgero.item.forgerotool.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


/**
 * Class for storing TemplateTextures for Tool Parts
 * This class can create recouloured versions of it's template data by using the createRecoulouredImage function.
 */
public class BaseTexture {
    private final ArrayList<PixelInformation> pixelValues;
    private final Integer[] greyScaleValues;

    private BaseTexture(ArrayList<PixelInformation> pixelValues, Integer[] greyScaleValues) {
        this.pixelValues = pixelValues;
        this.greyScaleValues = greyScaleValues;
    }

    public static BaseTexture createBaseTexture(BufferedImage templateImage) {
        ArrayList<PixelInformation> pixelValues = new ArrayList<>();
        HashSet<Integer> greyScaleValueSet = new HashSet<>();
        for (int y = 0; y < templateImage.getHeight(); ++y) {
            for (int x = 0; x < templateImage.getWidth(); ++x) {
                if (templateImage.getRGB(y, x) != 0) {
                    greyScaleValueSet.add(templateImage.getRGB(y, x));
                    pixelValues.add(new PixelInformation(y, x, templateImage.getRGB(y, x)));
                }
            }
        }
        Integer[] greyScaleValues = greyScaleValueSet.toArray(Integer[]::new);
        return new BaseTexture(pixelValues, greyScaleValues);
    }

    public BufferedImage createRecolouredImage(MaterialColourPalette templatePalette) {
        int paletteSize = templatePalette.getColourValues().length;
        int greyScaleSize = greyScaleValues.length;
        Integer[] palette = createUsableColourPalette(greyScaleSize, templatePalette);
        assert greyScaleSize >= 2 && paletteSize >= 2;

        BufferedImage recolouredImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        for (PixelInformation pixel : pixelValues) {
            recolouredImage.setRGB(pixel.heightIndex(), pixel.getLengthIndex(), palette[Arrays.asList(greyScaleValues).indexOf(pixel.getRgbColor())]);
        }
        return recolouredImage;
    }

    /**
     * Method for normalizing the colour palette to match the existing greyscale values of the template texture
     *
     * @return A colour palette matching the original greyscale values
     */
    private Integer[] createUsableColourPalette(int greyScaleSize, MaterialColourPalette palette) {
        Integer[] colourList = new Integer[greyScaleSize];
        for (int i = 0; i < colourList.length; i++) {
            float scaleValue = (float) i / (float) palette.getColourValues().length;
            float normalized = scaleValue * greyScaleSize;
            int newIndex = Math.round(normalized);

            if (newIndex == 0) {
                colourList[0] = palette.getColourValues()[0];
            } else if (newIndex == colourList.length - 1) {
                colourList[colourList.length - 1] = palette.getColourValues()[colourList.length - 1];
            } else {
                colourList[i] = palette.getColourValues()[newIndex];
            }
        }
        return colourList;
    }
}

