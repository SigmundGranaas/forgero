package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.feature;

import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class VeinMiningWriter extends FeatureWriter {
    public VeinMiningWriter(PropertyData data) {
        super(data);
    }

    @Override
    protected List<Text> writeExtendedData() {
        List<Text> entries = new ArrayList<>();
        entries.add(writeTextWithValue("depth", super.data.getValue()));
        return entries;
    }
}
