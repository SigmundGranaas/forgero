package com.sigmundgranaas.forgero.client.forgerotool.texture.template;

import com.sigmundgranaas.forgero.client.forgerotool.texture.utils.RgbColour;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Class for storing TemplateTextures for Tool Parts
 * This class can create recouloured versions of its template data by using the createRecoulouredImage function.
 */
public class TemplateTexture {
    private final ArrayList<PixelInformation> pixelValues;
    private final List<RgbColour> greyScaleValues;

    private TemplateTexture(ArrayList<PixelInformation> pixelValues, List<RgbColour> greyScaleValues) {
        this.pixelValues = pixelValues;
        this.greyScaleValues = greyScaleValues;
    }

    public static TemplateTexture createBaseTexture(BufferedImage templateImage) {
        ArrayList<PixelInformation> pixelValues = new ArrayList<>();
        HashSet<RgbColour> greyScaleValueSet = new HashSet<>();
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
        return new TemplateTexture(pixelValues, greyScaleValues);
    }

    public List<RgbColour> getGreyScaleValues() {
        return greyScaleValues;
    }

    public ArrayList<PixelInformation> getPixelValues() {
        return pixelValues;
    }
}

