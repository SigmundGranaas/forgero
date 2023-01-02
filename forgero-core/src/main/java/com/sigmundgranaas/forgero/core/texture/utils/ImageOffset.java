package com.sigmundgranaas.forgero.core.texture.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class ImageOffset {

    public static BufferedImage applyOffset(BufferedImage input, List<Offset> offsetList) {
        BufferedImage newImage = new BufferedImage(input.getWidth(), input.getHeight(), TYPE_INT_ARGB);
        var offset = offsetList.stream().reduce(new Offset(0, 0), (offset1, offset2) -> new Offset(offset1.x() + offset2.x(), offset1.y() + offset2.y()));
        int transparent = new Color(0, 0, 0, 0).getRGB();
        for (int i = 0; i < input.getWidth(); i++) {
            for (int j = 0; j < input.getHeight(); j++) {
                var currentPixel = new Offset(i, j);
                var newPixel = currentPixel.apply(offset);
                if (newPixel.x() >= 0 && newPixel.x() < input.getWidth() && newPixel.y() >= 0 && newPixel.y() < input.getHeight()) {
                    newImage.setRGB(i, j, input.getRGB((int) newPixel.x(), (int) newPixel.y()));
                } else {
                    newImage.setRGB(i, j, transparent);
                }
            }
        }
        return newImage;
    }
}
