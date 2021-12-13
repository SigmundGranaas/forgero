package com.sigmundgranaas.forgero.item.forgerotool.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * Class for storing TemplateTextures for Tool Parts
 * This class can create recouloured versions of it's template data by using the createRecoulouredImage function.
 */
public class BaseTexture {
    private final ArrayList<PixelInformation> pixelValues;
    private final int[] greyScaleValues;

    private BaseTexture(ArrayList<PixelInformation> pixelValues, int[] greyScaleValues) {
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
        int[] greyScaleValues = greyScaleValueSet.stream().mapToInt(Integer::intValue).toArray();
        greyScaleValues = MaterialColourPalette.sortRgbValues(greyScaleValues);
        return new BaseTexture(pixelValues, greyScaleValues);
    }

    public BufferedImage createRecolouredImage(MaterialColourPalette templatePalette) {
        int paletteSize = templatePalette.getColourValues().length;
        int greyScaleSize = greyScaleValues.length;
        int[] palette = createUsableColourPalette(greyScaleSize, templatePalette);
        assert greyScaleSize >= 2 && paletteSize >= 2;

        BufferedImage recolouredImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (PixelInformation pixel : pixelValues) {
            recolouredImage.setRGB(pixel.heightIndex(), pixel.getLengthIndex(), palette[findIntPosition(pixel.getRgbColor(), greyScaleValues)]);
        }
        return recolouredImage;
    }

    public int[] getGreyScaleValues() {
        return greyScaleValues;
    }

    public int findIntPosition(int target, int[] reference) {
        for (int i = 0; i < reference.length; i++) {
            if (target == reference[i]) {
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
    private int[] createUsableColourPalette(int greyScaleSize, MaterialColourPalette palette) {
        int[] colourList = new int[greyScaleSize];
        if (greyScaleSize == palette.getColourValues().length) {
            for (int i = 0; i < colourList.length; i++) {
                colourList[i] = palette.getColourValues()[i];
            }
            return colourList;
        }

        for (int i = 0; i < colourList.length; i++) {
            float scaleValue = (float) palette.getColourValues().length / (float) greyScaleSize;
            float normalized = scaleValue * i;
            int newIndex = Math.round(normalized);

            if (newIndex == 0 || i == 0) {
                colourList[0] = palette.getColourValues()[0];
            } else if (i == 1) {
                colourList[1] = palette.getColourValues()[1];
            } else {
                colourList[i] = palette.getColourValues()[newIndex];
            }
        }
        return colourList;
    }
}

