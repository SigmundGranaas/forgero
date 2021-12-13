package com.sigmundgranaas.forgero.item.forgerotool.model;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MaterialColourPalette {
    private final int[] colourValues;

    private MaterialColourPalette(int[] colourValues) {
        this.colourValues = colourValues;
    }

    public static MaterialColourPalette createColourPalette(List<BufferedImage> palettes) {
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
        int[] colourValues = colourValueSet.stream().mapToInt(Integer::intValue).toArray();
        colourValues = sortRgbValues(colourValues);
        return new MaterialColourPalette(colourValues);
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

    public int[] getColourValues() {
        return colourValues;
    }
}
