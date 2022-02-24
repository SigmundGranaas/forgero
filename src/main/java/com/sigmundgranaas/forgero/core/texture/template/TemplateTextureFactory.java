package com.sigmundgranaas.forgero.core.texture.template;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.TemplateTextureIdentifier;
import com.sigmundgranaas.forgero.core.texture.Texture;
import com.sigmundgranaas.forgero.core.texture.utils.RgbColour;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for extracting a TemplateTexture from a raw image.
 */
public class TemplateTextureFactory {
    /**
     *
     * Method for parsing raw images and extracting every pixel which is not transparent and storing this information inside a TemplateTexture.
     *
     * @param texture The raw Template texture
     * @param id belonging to the texture being generated
     * @return The generated TemplateTexture
     */
    public TemplateTexture createTemplateTexture(Texture texture, TemplateTextureIdentifier id) {
        ArrayList<PixelInformation> pixelValues = new ArrayList<>();
        HashSet<RgbColour> greyScaleValueSet = new HashSet<>();
        BufferedImage templateImage = texture.getImage();

        for (int y = 0; y < templateImage.getHeight(); ++y) {
            for (int x = 0; x < templateImage.getWidth(); ++x) {
                if (templateImage.getRGB(x, y) != 0) {
                    greyScaleValueSet.add(new RgbColour(templateImage.getRGB(x, y)));
                    pixelValues.add(new PixelInformation(x, y, new RgbColour(templateImage.getRGB(x, y))));
                }
            }
        }

        List<RgbColour> greyScaleValues = greyScaleValueSet.stream().toList();
        greyScaleValues = greyScaleValues.stream().sorted().collect(Collectors.toList());
        return new TemplateTexture(pixelValues, greyScaleValues, id);
    }
}
