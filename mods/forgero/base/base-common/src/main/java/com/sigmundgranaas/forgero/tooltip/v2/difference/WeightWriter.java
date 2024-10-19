package com.sigmundgranaas.forgero.tooltip.v2.difference;

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
}
