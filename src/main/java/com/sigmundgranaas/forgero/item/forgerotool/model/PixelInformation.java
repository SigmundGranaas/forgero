package com.sigmundgranaas.forgero.item.forgerotool.model;

public record PixelInformation(int heightIndex, int lengthIndex, int rgbColor) {

    public int getHeightIndex() {
        return heightIndex;
    }

    public int getLengthIndex() {
        return lengthIndex;
    }

    public int getRgbColor() {
        return rgbColor;
    }
}
