package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.feature;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.text.Text;

public class VeinMiningWriter extends FeatureWriter {
	public VeinMiningWriter(PropertyData data) {
		super(data);
	}

	@Override
	protected List<Text> writeExtendedData() {
		List<Text> entries = new ArrayList<>();
		entries.add(writeTextWithValue("depth", super.data.getValue()));
		entries.addAll(super.writeExtendedData());
		return entries;
	}
}
