package com.sigmundgranaas.forgero.core.texture.V2.recolor;

import com.sigmundgranaas.forgero.core.texture.V2.Palette;
import com.sigmundgranaas.forgero.core.texture.V2.TemplateTexture;

import java.awt.image.BufferedImage;

public interface RecolorStrategy {
	BufferedImage recolor(TemplateTexture templateTexture, Palette palette);
}
