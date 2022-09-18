
package com.sigmundgranaas.forgero.texture.template;


import com.sigmundgranaas.forgero.texture.utils.RgbColour;

/**
 * PixelInformation is used to store information about where each template rgb pixel is placed inside a texture.
 * This class should not contain any transparent pixels.
 */
public record PixelInformation(int lengthIndex, int heightIndex, RgbColour rgbColor) {

    public int getHeightIndex() {
        return heightIndex;
    }

    public int getLengthIndex() {
        return lengthIndex;
    }

    public RgbColour getRgbColor() {
        return rgbColor;
    }
}
