package com.sigmundgranaas.forgero.texture.palette.strategy;

import com.sigmundgranaas.forgero.texture.palette.Palette;
import com.sigmundgranaas.forgero.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.texture.utils.RgbColour;

import java.util.ArrayList;
import java.util.List;

public class SecondaryToolPartRecolourStrategy extends DefaultToolPartRecolourStrategy {
    public SecondaryToolPartRecolourStrategy(TemplateTexture template, Palette palette) {
        super(template, palette);
    }

    @Override
    public List<RgbColour> createUsableColourPalette() {
        int greyScaleSize = template.getGreyScaleValues().size();

        List<RgbColour> colourList = new ArrayList<>(greyScaleSize);
        if (greyScaleSize == palette.getColourValues().size()) {
            for (int i = 0; i < greyScaleSize; i++) {
                colourList.add(palette.getColourValues().get(i));
            }
            return colourList;
        }

        for (int i = 1; i < greyScaleSize + 1; i++) {
            float scaleValue = (float) palette.getColourValues().size() / (float) greyScaleSize;
            float normalized = scaleValue * i;
            int newIndex = Math.round(normalized) - 1;

            colourList.add(i - 1, palette.getColourValues().get(newIndex));
        }
        return colourList;
    }

}
