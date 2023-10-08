package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.feature;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.text.Text;


public class PatternMiningWriter extends FeatureWriter {
	public PatternMiningWriter(PropertyData data) {
		super(data);
	}

	@Override
	protected List<Text> writeExtendedData() {
		List<Text> entries = new ArrayList<>();
		entries.add(writeTextWithValue("area", String.format("%sx%s", data.getPattern().length, data.getPattern()[0].length())));
		entries.addAll(super.writeExtendedData());
		return entries;
	}
}
