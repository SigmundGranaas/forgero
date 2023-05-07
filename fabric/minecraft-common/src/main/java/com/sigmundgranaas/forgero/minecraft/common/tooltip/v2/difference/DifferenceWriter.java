package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.difference;

import static com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.AttributeWriterHelper.number;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.minecraft.common.utils.Text;

import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public interface DifferenceWriter {
	private static String plus(float number) {
		return number > 0 ? "+" : "";
	}

	default Formatting getDifferenceFormatting(float difference) {
		if (difference > 0) {
			return Formatting.GREEN;
		} else if (difference < -0) {
			return Formatting.RED;
		} else {
			return Formatting.WHITE;
		}
	}

	default MutableText getArrow(float difference) {
		if (difference > 0) {
			return Text.literal("↑");
		} else if (difference < 0) {
			return Text.literal("↓");
		} else {
			return Text.empty();
		}
	}

	default MutableText getDifferenceMarker(float difference) {
		if (difference != 0) {
			MutableText marker = getArrow(difference);

			if (ForgeroConfigurationLoader.configuration.showAttributeDifference) {
				marker.append(Text.literal(" (" + plus(difference) + number(difference) + ")"));
			}

			return marker;
		} else {
			return Text.empty();
		}
	}
}
