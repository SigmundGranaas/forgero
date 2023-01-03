package com.sigmundgranaas.forgero.core.texture.palette;

import com.sigmundgranaas.forgero.core.texture.palette.material.MaterialPalette;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.texture.Texture;
import com.sigmundgranaas.forgero.core.texture.utils.RgbColour;

import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for extracting unique color values from textures and creating a palette from these images.
 * <p>
 * This supports exclusion images as well.
 * This means that values present in the exclusion images will be removed in the final palette.
 * Excluding images allows you to use textures from existing tools and avoiding creating a palette which includes the brown colours from the handle of a tool, or the oar of a boat texture.
 */
public class PaletteFactoryImpl implements PaletteFactory {
    //TODO Split into smaller methods
    
    public static MaterialPalette createColourPalette(UnbakedPalette palette) {
        HashSet<RgbColour> colourValueSet = new HashSet<>();
        for (Texture image : palette.getInclusions()) {
            extractColourSetFromImage(colourValueSet, image);
        }

        HashSet<RgbColour> exclusionValueSet = new HashSet<>();
        for (Texture image : palette.getExclusions()) {
            extractColourSetFromImage(exclusionValueSet, image);
        }
        RgbColour blackPoint = colourValueSet
                .stream()
                .min(Comparator.comparing(RgbColour::getLightValue))
                .orElse(new RgbColour(255, 255, 255));

        exclusionValueSet.remove(blackPoint);

        colourValueSet.removeAll(exclusionValueSet);

        List<RgbColour> colourValues = colourValueSet.stream().toList();
        colourValues = colourValues.stream().sorted().collect(Collectors.toList());
        return new MaterialPalette(colourValues, palette.getId());
    }

    private static void extractColourSetFromImage(HashSet<RgbColour> colourValueSet, Texture texture) {
        BufferedImage image = texture.getImage();
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                if (image.getRGB(x, y) != 0) {
                    colourValueSet.add(new RgbColour(image.getRGB(x, y)));
                }
            }
        }
    }


    public Palette createColourPaletteFromExistingPalette(BufferedImage palette, PaletteIdentifier id) {
        HashSet<RgbColour> colourValueSet = new HashSet<>();

        for (int x = 0; x < palette.getWidth(); ++x) {
            if (palette.getRGB(x, 0) != 0) {
                colourValueSet.add(new RgbColour(palette.getRGB(x, 0)));
            }
        }

        List<RgbColour> colourValues = colourValueSet.stream().toList();
        colourValues = colourValues.stream().sorted().collect(Collectors.toList());
        return new MaterialPalette(colourValues, id);
    }

    @Override
    public Palette createPalette(UnbakedPalette palette) {
        return createColourPalette(palette);
    }

    @Override
    public Palette createPalette(BufferedImage palette, PaletteIdentifier id) {
        return createColourPaletteFromExistingPalette(palette, id);
    }
}
