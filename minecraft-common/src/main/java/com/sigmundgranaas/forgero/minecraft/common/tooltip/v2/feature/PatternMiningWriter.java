package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.feature;

import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;


public class PatternMiningWriter extends FeatureWriter {
    public PatternMiningWriter(PropertyData data) {
        super(data);
    }

    @Override
    protected MutableText writeDataHeader() {
        return super.writeDataHeader().append(separator()).append(Text.literal(String.format(" %sx%s", data.getPattern().length, data.getPattern()[0].length()).formatted(highlighted())));
    }
}
