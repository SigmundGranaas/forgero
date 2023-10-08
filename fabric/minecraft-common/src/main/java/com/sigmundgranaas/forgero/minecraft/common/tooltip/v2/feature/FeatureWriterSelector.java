package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.feature;

import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.ToolBlockHandler.BLOCK_BREAKING_PATTERN_KEY;
import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.ToolBlockHandler.VEIN_MINING_KEY;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;

public class FeatureWriterSelector {
	private final Map<String, Function<PropertyData, FeatureWriter>> writerFactory;

	public FeatureWriterSelector() {
		this.writerFactory = new HashMap<>();
	}

	public static FeatureWriterSelector defaultSelector() {
		var selector = new FeatureWriterSelector();
		selector.add(VEIN_MINING_KEY, VeinMiningWriter::new);
		selector.add("EFFECTIVE_BLOCKS", BlockEffectivenessWriter::new);
		selector.add(BLOCK_BREAKING_PATTERN_KEY, PatternMiningWriter::new);
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
