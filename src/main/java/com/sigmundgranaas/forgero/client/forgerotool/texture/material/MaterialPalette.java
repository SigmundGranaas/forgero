package com.sigmundgranaas.forgero.client.forgerotool.texture.material;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MaterialPalette {
    private final int[] colourValues;
    private final int blackPoint;

    private MaterialPalette(int[] colourValues, int blackPoint) {
        this.colourValues = colourValues;
        this.blackPoint = blackPoint;
    }

    //TODO Split into smaller methods
    public static MaterialPalette createColourPalette(List<BufferedImage> palettes, List<BufferedImage> exlusions) {
        HashSet<Integer> colourValueSet = new HashSet<>();
        for (BufferedImage image : palettes) {
            for (int y = 0; y < image.getHeight(); ++y) {
                for (int x = 0; x < image.getWidth(); ++x) {
                    if (image.getRGB(y, x) != 0) {
                        colourValueSet.add(image.getRGB(y, x));
                    }
                }
            }
        }

        HashSet<Integer> exclusionValueSet = new HashSet<>();
        for (BufferedImage image : exlusions) {
            for (int y = 0; y < image.getHeight(); ++y) {
                for (int x = 0; x < image.getWidth(); ++x) {
                    if (image.getRGB(y, x) != 0) {
                        exclusionValueSet.add(image.getRGB(y, x));
                    }
                }
            }
        }
        int blackPoint = sortRgbValues(colourValueSet.stream().mapToInt(Integer::intValue).toArray())[0];
        exclusionValueSet.remove(blackPoint);
        colourValueSet.removeAll(exclusionValueSet);
        int[] colourValues = colourValueSet.stream().mapToInt(Integer::intValue).toArray();
        //int[] exclusionValues = exclusionValueSet.stream().mapToInt(Integer::intValue).toArray();
        colourValues = sortRgbValues(colourValues);
        colourValues[1] = getSecondaryDarkPoint(colourValues);
        return new MaterialPalette(colourValues, blackPoint);
    }

    public static int[] sortRgbValues(int[] rgbValues) {
        Integer[] integerArray = Arrays.stream(rgbValues).boxed().toArray(Integer[]::new);
        return Arrays.stream(integerArray).sorted((prev, next) -> {
            int redprev = (prev >> 16) & 0xFF;
            int blueprev = (prev >> 8) & 0xFF;
            int greenprev = prev & 0xFF;
            int previousLightValue = (redprev + blueprev + greenprev) / 3;

            int rednext = (next >> 16) & 0xFF;
            int bluenext = (next >> 8) & 0xFF;
            int greennext = next & 0xFF;
            int nextLightValue = (rednext + bluenext + greennext) / 3;
            return previousLightValue - nextLightValue;
        }).mapToInt(Integer::intValue).toArray();
    }

    public static int getSecondaryDarkPoint(int[] sortedColourValues) {
        int blackPoint = sortedColourValues[0];
        int secondaryPoint = sortedColourValues[1];

        int blackPointRed = (blackPoint >> 16) & 0xFF;
        int blackPointBlue = (blackPoint >> 8) & 0xFF;
        int blackPointGreen = blackPoint & 0xFF;
        int blackPointLightValue = (blackPointRed + blackPointBlue + blackPointGreen) / 3;

        int secondaryPointRed = (secondaryPoint >> 16) & 0xFF;
        int secondaryPointBlue = (secondaryPoint >> 8) & 0xFF;
        int secondaryPointGreen = secondaryPoint & 0xFF;
        int secondaryPointLightValue = (secondaryPointRed + secondaryPointBlue + secondaryPointGreen) / 3;

        int secondaryDarkPoint = sortedColourValues[1];

        if (secondaryPointLightValue - blackPointLightValue > 20) {

            int thirdPoint = sortedColourValues[2];
            int thirdPointRed = (thirdPoint >> 16) & 0xFF;
            int thirdPointBlue = (thirdPoint >> 8) & 0xFF;
            int thirdPointGreen = thirdPoint & 0xFF;
            Color secondaryRGBPoint = new Color((thirdPointRed + blackPointRed) / 2, (thirdPointBlue + blackPointBlue) / 2, (thirdPointGreen + blackPointGreen) / 2);
            secondaryDarkPoint = secondaryRGBPoint.getRGB();
        }
        return secondaryDarkPoint;
    }

    public int[] getColourValues() {
        return colourValues;
    }
}


