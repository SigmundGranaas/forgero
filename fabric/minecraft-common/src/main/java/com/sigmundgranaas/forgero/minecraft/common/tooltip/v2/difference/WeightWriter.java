package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.difference;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WeightWriter implements DifferenceWriter {
	@Override
	public Formatting getDifferenceFormatting(float difference) {
		if (difference < 0) {
			return Formatting.GREEN;
		} else if (difference > 0) {
			return Formatting.RED;
		} else {
			return Formatting.WHITE;
		}
	}

	@Override
	public MutableText getArrow(float difference) {
		if (difference < 0) {
			return Text.literal("↑");
		} else if (difference > 0) {
			return Text.literal("↓");
		} else {
			return Text.empty();
		}
	}
}
