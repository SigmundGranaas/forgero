package com.sigmundgranaas.forgero.common.tooltip.display;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * A utility class for creating and formatting Text components for tooltips.
 * This centralizes all formatting logic, including locale-safe number formatting,
 * to ensure a consistent look and feel.
 */
public final class TooltipTextFormatter {

	// ThreadLocal DecimalFormat for thread safety (DecimalFormat is not thread-safe).
	// Using Locale.US ensures the decimal separator is always a period (.).
	private static final ThreadLocal<DecimalFormat> NUMBER_FORMAT = ThreadLocal.withInitial(
			() -> new DecimalFormat("#.##", new java.text.DecimalFormatSymbols(Locale.US))
	);

	/**
	 * Formats a float to a string with a maximum of two decimal places, using a period as the separator.
	 *
	 * @param value The float to format.
	 * @return A locale-invariant string representation of the number.
	 */
	public static String formatFloat(float value) {
		if (value == (long) value) {
			return String.format(Locale.US, "%d", (long) value);
		}
		return NUMBER_FORMAT.get().format(value);
	}

	/**
	 * Creates a standard, gray, translatable section header (e.g., "Attributes:").
	 *
	 * @param translationKey The translation key for the section name (e.g., "tooltip.forgero.attributes").
	 * @return A formatted, mutable text component for the section header.
	 */
	public static MutableText createSectionHeader(String translationKey) {
		return Text.translatable(translationKey)
				.append(":")
				.formatted(Formatting.GRAY);
	}

	/**
	 * Creates a text component for a single attribute line within a section.
	 * Example: " Attack Damage: +5"
	 *
	 * @param attributeKey The translation key for the attribute's name.
	 * @param valueText    The pre-formatted value of the attribute as a Text component.
	 * @return A fully assembled and formatted line of text, indented with one space.
	 */
	public static MutableText createAttributeLine(String attributeKey, Text valueText) {
		return Text.literal(" ")
				.append(Text.translatable(attributeKey).formatted(Formatting.DARK_GREEN))
				.append(": ")
				.append(valueText);
	}

	/**
	 * Creates a Text component for a numeric value, styled based on its sign.
	 *
	 * @param value        The numeric value.
	 * @param positiveFormat The formatting to apply for positive numbers.
	 * @param negativeFormat The formatting to apply for negative numbers.
	 * @param prefix       A prefix to add before the number (e.g., "+").
	 * @param suffix       A suffix to add after the number (e.g., "%").
	 * @return A formatted Text component representing the value.
	 */
	public static MutableText createValueText(float value, Formatting positiveFormat, Formatting negativeFormat, String prefix, String suffix) {
		String formattedValue = formatFloat(value);
		Formatting format = value >= 0 ? positiveFormat : negativeFormat;
		return Text.literal(prefix + formattedValue + suffix).formatted(format);
	}
}
