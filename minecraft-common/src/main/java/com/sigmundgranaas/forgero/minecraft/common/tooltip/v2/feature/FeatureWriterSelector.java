package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.feature;

import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.ToolBlockHandler.VEIN_MINING_KEY;

public class FeatureWriterSelector {
    private final Map<String, Function<PropertyData, FeatureWriter>> writerFactory;

    public FeatureWriterSelector() {
        this.writerFactory = new HashMap<>();
    }

    public static FeatureWriterSelector defaultSelector() {
        var selector = new FeatureWriterSelector();
        selector.add(VEIN_MINING_KEY, VeinMiningWriter::new);
        selector.add("EFFECTIVE_BLOCKS", BlockEffectivenessWriter::new);
        return selector;
    }

    public FeatureWriterSelector add(String type, Function<PropertyData, FeatureWriter> factory) {
        writerFactory.put(type, factory);
        return this;
    }

    public FeatureWriter writer(PropertyData data, TooltipConfiguration configuration) {
        var FeatureConfig = configuration.toBuilder();
        if (!configuration.hideSectionTitle()) {
            FeatureConfig.baseIndent(configuration.baseIndent() + 1);
        }

        if (writerFactory.containsKey(data.type())) {
            return writerFactory.get(data.type()).apply(data).setConfig(FeatureConfig.build());
        }
        return new FeatureWriter(data).setConfig(FeatureConfig.build());
    }
}
