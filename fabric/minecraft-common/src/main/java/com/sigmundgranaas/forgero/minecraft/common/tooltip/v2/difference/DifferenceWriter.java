package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.difference;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface DifferenceWriter {
	default Formatting getDifferenceFormatting(float difference) {
		if (difference > 1.1) {
			return Formatting.DARK_GREEN;
		} else if (difference > 0.6) {
			return Formatting.GREEN;
		} else if (difference > 0.2) {
			return Formatting.YELLOW;
		} else if (difference < -1) {
			return Formatting.DARK_RED;
		} else if (difference < -0.6) {
			return Formatting.RED;
		} else if (difference < -0.2) {
			return Formatting.GOLD;
		} else {
			return Formatting.WHITE;
		}
	}

	default MutableText getDifferenceMarker(float difference) {
		if (difference > 0) {
			return Text.literal("↑");
		} else if (difference < 0) {
			return Text.literal("↓");
		} else {
			return Text.empty();
		}
	}
}
