package com.sigmundgranaas.forgero.common.tooltip.section;

import com.sigmundgranaas.forgero.common.tooltip.display.AttributeDisplayData;
import com.sigmundgranaas.forgero.common.tooltip.display.TooltipTextFormatter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A dedicated writer for creating tooltip lines for final, aggregated attribute values.
 * It formats a single line of text representing the attribute's final computed value.
 */
public class SimpleAttributeWriter {

	public List<Text> createTooltipLines(AttributeDisplayData displayData, float rawValue) {
		if (displayData.isDefault(rawValue)) {
			return Collections.emptyList();
		}

		float displayValue = displayData.getDisplayValue(rawValue);
		Optional<Text> text = createText(displayData, displayValue);

		return text.map(List::of).orElse(Collections.emptyList());
	}

	private Optional<Text> createText(AttributeDisplayData displayData, float value) {
		MutableText valueText;
		String valueStr = TooltipTextFormatter.formatFloat(value);

		switch (displayData.style()) {
			case ADDITIVE -> {
				String prefix = value > 0 ? "+" : "";
				valueText = TooltipTextFormatter.createValueText(value, Formatting.BLUE, Formatting.RED, prefix, "");
			}
			case PERCENTAGE -> {
				String prefix = value > 0 ? "+" : "";
				valueText = TooltipTextFormatter.createValueText(value, Formatting.BLUE, Formatting.RED, prefix, "%");
			}
			case BASE_VALUE -> valueText = Text.literal(valueStr).formatted(Formatting.GOLD);
			default -> {
				return Optional.empty();
			}
		}
		return Optional.of(TooltipTextFormatter.createAttributeLine(displayData.translationKey(), valueText));
	}
}
