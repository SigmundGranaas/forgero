package com.sigmundgranaas.forgero.texture.V2;

import com.sigmundgranaas.forgero.texture.utils.RgbColour;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Palette implements Texture {
    private final BufferedImage palette;

    public Palette(BufferedImage palette) {
        this.palette = palette;
    }

    @Override
    public InputStream getStream() throws IOException {
        return Texture.imageToStream(palette);
    }

    @Override
    public BufferedImage getImage() {
        return palette;
    }

    public List<RgbColour> getColourValues() {
        return paletteFromImage(palette);
    }


    public List<RgbColour> paletteFromImage(BufferedImage palette) {
        HashSet<RgbColour> colourValueSet = new HashSet<>();

        for (int x = 0; x < palette.getWidth(); ++x) {
            if (palette.getRGB(x, 0) != 0) {
                colourValueSet.add(new RgbColour(palette.getRGB(x, 0)));
            }
        }

        List<RgbColour> colourValues = colourValueSet.stream().toList();
        return colourValues.stream().sorted().collect(Collectors.toList());
    }
}
