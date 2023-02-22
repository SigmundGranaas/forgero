package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.feature;

import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import net.minecraft.text.MutableText;

public class BlockEffectivenessWriter extends FeatureWriter {
    public BlockEffectivenessWriter(PropertyData data) {
        super(data);
    }

    @Override
    protected MutableText writeDataHeader() {
        return indented(2).append(writeTextWithInfo("feature.effective_blocks", "tag." + data.getTags().get(0).replace(":", ".").replace("/", ".")));
    }
}
