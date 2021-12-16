package com.sigmundgranaas.forgero.client.forgerotool.texture.material;

import com.sigmundgranaas.forgero.client.forgerotool.texture.utils.RgbColour;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MaterialPalette {
    private final List<RgbColour> colourValues;

    private MaterialPalette(List<RgbColour> colourValues) {
        this.colourValues = colourValues;
    }

    //TODO Split into smaller methods
    public static MaterialPalette createColourPalette(List<BufferedImage> palettes, List<BufferedImage> exlusions) {
        HashSet<RgbColour> colourValueSet = new HashSet<>();
        for (BufferedImage image : palettes) {
            extractColourSetFromImage(colourValueSet, image);
        }

        HashSet<RgbColour> exclusionValueSet = new HashSet<>();
        for (BufferedImage image : exlusions) {
            extractColourSetFromImage(exclusionValueSet, image);
        }
        RgbColour blackPoint = colourValueSet.stream().toList().get(0);
        exclusionValueSet.remove(blackPoint);

        colourValueSet.removeAll(exclusionValueSet);

        List<RgbColour> colourValues = colourValueSet.stream().toList();
        colourValues = colourValues.stream().sorted().collect(Collectors.toList());
        return new MaterialPalette(colourValues);
    }

    private static void extractColourSetFromImage(HashSet<RgbColour> colourValueSet, BufferedImage image) {
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                if (image.getRGB(x, y) != 0) {
                    colourValueSet.add(new RgbColour(image.getRGB(x, y)));
                }
            }
        }
    }

    public static MaterialPalette createColourPaletteFromExistingPalette(BufferedImage palette) {
        HashSet<RgbColour> colourValueSet = new HashSet<>();

        for (int x = 0; x < palette.getWidth(); ++x) {
            if (palette.getRGB(x, 0) != 0) {
                colourValueSet.add(new RgbColour(palette.getRGB(x, 0)));
            }
        }

        List<RgbColour> colourValues = colourValueSet.stream().toList();
        colourValues = colourValues.stream().sorted().collect(Collectors.toList());
        return new MaterialPalette(colourValues);
    }

    public List<RgbColour> getColourValues() {
        return colourValues;
    }
}


