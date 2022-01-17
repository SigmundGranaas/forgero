package com.sigmundgranaas.forgero.core.texture.palette.material;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.texture.Texture;
import com.sigmundgranaas.forgero.core.texture.palette.Palette;
import com.sigmundgranaas.forgero.core.texture.utils.RgbColour;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MaterialPalette implements Palette, Texture {
    private final List<RgbColour> colourValues;
    private final PaletteIdentifier id;

    public MaterialPalette(List<RgbColour> colourValues, PaletteIdentifier id) {
        this.colourValues = colourValues;
        this.id = id;
    }

    public List<RgbColour> getColourValues() {
        return colourValues;
    }

    @Override
    public InputStream getStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(getImage(), "png", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        os.close();
        return is;
    }

    @Override
    public TextureIdentifier getId() {
        return id;
    }

    @Override
    public BufferedImage getImage() {
        BufferedImage paletteImage = new BufferedImage(getColourValues().size(), 1, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < getColourValues().size(); x++) {
            paletteImage.setRGB(x, 0, getColourValues().get(x).getRgb());
        }
        return paletteImage;
    }
}


