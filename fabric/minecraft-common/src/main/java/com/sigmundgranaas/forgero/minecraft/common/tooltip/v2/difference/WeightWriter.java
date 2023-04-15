package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.difference;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WeightWriter implements DifferenceWriter {
	@Override
	public Formatting getDifferenceFormatting(float difference) {
		if (difference > 30) {
			return Formatting.DARK_RED;
		} else if (difference > 10) {
			return Formatting.RED;
		} else if (difference > 2) {
			return Formatting.GOLD;
		} else if (difference < -20) {
			return Formatting.DARK_GREEN;
		} else if (difference < -10) {
			return Formatting.GREEN;
		} else if (difference < -2) {
			return Formatting.YELLOW;
		} else {
			return Formatting.WHITE;
		}
	}

	@Override
	public MutableText getDifferenceMarker(float difference) {
		if (difference < 0) {
			return Text.literal("⌃");
		} else if (difference > 0) {
			return Text.literal("⌄");
		} else {
			return Text.empty();
		}
	}
}
