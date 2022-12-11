package com.sigmundgranaas.forgero.texture.palette;

import com.sigmundgranaas.forgero.texture.utils.RgbColour;

import java.util.List;

/**
 * Interface to represent a strip of color values which should correspond to a Palette
 */
public interface Palette {
    List<RgbColour> getColourValues();
}
