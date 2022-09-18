package com.sigmundgranaas.forgero.texture.palette.strategy;

import com.sigmundgranaas.forgero.texture.palette.Palette;
import com.sigmundgranaas.forgero.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.texture.utils.RgbColour;

import java.util.ArrayList;
import java.util.List;

public class PickaxeHeadRecolourStrategy extends DefaultToolPartRecolourStrategy {
    public PickaxeHeadRecolourStrategy(TemplateTexture template, Palette palette) {
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

        for (int i = 0; i < greyScaleSize; i++) {
            float scaleValue = (float) palette.getColourValues().size() / (float) greyScaleSize;
            float normalized = scaleValue * i;
            int newIndex = Math.round(normalized);

            if (newIndex == 0 || i == 0) {
                colourList.add(0, palette.getColourValues().get(0));
            } else if (newIndex == 1 || i == 1) {
                colourList.add(1, palette.getColourValues().get(1));
            } else {
                colourList.add(i, palette.getColourValues().get(newIndex));
            }
        }
        return colourList;
    }
}
