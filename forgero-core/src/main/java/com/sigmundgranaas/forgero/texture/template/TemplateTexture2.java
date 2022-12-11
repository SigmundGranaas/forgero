package com.sigmundgranaas.forgero.texture.template;

import com.sigmundgranaas.forgero.identifier.texture.TemplateIdentifier;
import com.sigmundgranaas.forgero.texture.Texture;
import com.sigmundgranaas.forgero.texture.utils.RgbColour;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TemplateTexture2 implements Texture {
    private final List<PixelInformation> pixelValues;
    private final List<RgbColour> greyScaleValues;
    private final TemplateIdentifier id;

    public TemplateTexture2(ArrayList<PixelInformation> pixelValues, List<RgbColour> greyScaleValues, TemplateIdentifier id) {
        this.pixelValues = pixelValues;
        this.greyScaleValues = greyScaleValues;
        this.id = id;
    }


    public List<RgbColour> getGreyScaleValues() {
        return greyScaleValues;
    }

    public List<PixelInformation> getPixelValues() {
        return pixelValues;
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
    public TemplateIdentifier getId() {
        return id;
    }

    @Override
    public BufferedImage getImage() {
        BufferedImage paletteImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (PixelInformation pixel : pixelValues) {
            paletteImage.setRGB(pixel.getLengthIndex(), pixel.getHeightIndex(), pixel.getRgbColor().getRgb());
        }
        return paletteImage;
    }
}
