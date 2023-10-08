package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.feature;

import net.minecraft.text.MutableText;

public class BlockEffectivenessWriter extends FeatureWriter {
	public BlockEffectivenessWriter(PropertyData data) {
		super(data);
	}

	@Override
	protected MutableText writeDataHeader() {
		return indented(config.baseIndent()).append(writeTextWithInfo("feature.effective_blocks", "tag." + data.getTags().get(0).replace(":", ".").replace("/", ".")));
	}
}
