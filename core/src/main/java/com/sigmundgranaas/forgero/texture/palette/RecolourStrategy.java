package com.sigmundgranaas.forgero.texture.palette;

import java.awt.image.BufferedImage;

/**
 * Strategies are responsible for deciding how colour values in a palette should be selected when colorizing a template image.
 * <p>
 * Some textures contain very few greyscale values, and choosing the darkest values, like needed for the outline of a tool, might look awful when colorizing a gem.
 * Picking the right strategy can really enhance the look of a texture.
 */
public interface RecolourStrategy {
    BufferedImage recolour();
}
