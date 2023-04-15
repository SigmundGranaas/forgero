package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.difference;

import net.minecraft.util.Formatting;

public class DurabilityWriter implements DifferenceWriter {
	@Override
	public Formatting getDifferenceFormatting(float difference) {
		if (difference > 500) {
			return Formatting.DARK_GREEN;
		} else if (difference > 150) {
			return Formatting.GREEN;
		} else if (difference > 50) {
			return Formatting.YELLOW;
		} else if (difference < -300) {
			return Formatting.DARK_RED;
		} else if (difference < -100) {
			return Formatting.RED;
		} else if (difference < -30) {
			return Formatting.GOLD;
		} else {
			return Formatting.WHITE;
		}
	}
}
