package com.sigmundgranaas.forgero.client.forgerotool.texture.template;


import com.sigmundgranaas.forgero.client.forgerotool.texture.utils.RgbColour;

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
