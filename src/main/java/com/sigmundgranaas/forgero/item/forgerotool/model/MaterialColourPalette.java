package com.sigmundgranaas.forgero.item.forgerotool.model;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;

public class MaterialColourPalette {
    private final Integer[] colourValues;

    private MaterialColourPalette(Integer[] colourValues) {
        this.colourValues = colourValues;
    }

    public static MaterialColourPalette createColourPalette(BufferedImage templateImage) {
        HashSet<Integer> colourValueSet = new HashSet<>();
        for (int y = 0; y < templateImage.getHeight(); ++y) {
            for (int x = 0; x < templateImage.getWidth(); ++x) {
                if (templateImage.getRGB(y, x) != 0) {
                    colourValueSet.add(templateImage.getRGB(y, x));
                }
            }
        }
        Integer[] colourValues = colourValueSet.toArray(Integer[]::new);
        colourValues = sortRgbValues(colourValues);
        return new MaterialColourPalette(colourValues);
    }

    public static Integer[] sortRgbValues(Integer[] rgbValues) {
        return Arrays.stream(rgbValues).sorted((previous, next) -> {
            int redprev = (previous >> 16) & 0xFF;
            int blueprev = (previous >> 8) & 0xFF;
            int greenprev = previous & 0xFF;
            int previousLightValue = (redprev + blueprev + greenprev) / 3;

            int rednext = (next >> 16) & 0xFF;
            int bluenext = (next >> 8) & 0xFF;
            int greennext = next & 0xFF;
            int nextLightValue = (rednext + bluenext + greennext) / 3;
            return previousLightValue - nextLightValue;
        }).toArray(Integer[]::new);
    }

    public Integer[] getColourValues() {
        return colourValues;
    }
}
