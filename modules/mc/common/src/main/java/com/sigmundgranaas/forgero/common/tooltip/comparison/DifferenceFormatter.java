package com.sigmundgranaas.forgero.common.tooltip.comparison;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tooltip.display.TooltipTextFormatter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Formats difference values with colors and arrows for tooltip display.
 * <p>
 * Supports:
 * <ul>
 *   <li>Color coding: GREEN for improvements, RED for downgrades</li>
 *   <li>Arrow indicators: ↑ for positive, ↓ for negative changes</li>
 *   <li>Inverse attributes: where lower values are better (weight, cooldown)</li>
 *   <li>Custom styles per attribute</li>
 * </ul>
 *
 * <h2>Registering Inverse Attributes</h2>
 * <pre>{@code
 * // Weight: lower is better
 * DifferenceFormatter.registerInverseAttribute(OpenIdentifier.parse("forgero:weight"));
 *
 * // Now positive weight differences will show as RED (bad)
 * }</pre>
 */
public class DifferenceFormatter {

	/**
	 * Attributes where lower values are better.
	 * These have inverted color logic (positive diff = red, negative diff = green).
	 */
	private static final Set<OpenIdentifier> INVERSE_ATTRIBUTES = ConcurrentHashMap.newKeySet();

	/**
	 * Custom styles per attribute.
	 */
	private static final Map<OpenIdentifier, DifferenceStyle> CUSTOM_STYLES = new ConcurrentHashMap<>();

	/**
	 * Threshold for considering a difference as "neutral" (no change).
	 */
	private static final float NEUTRAL_THRESHOLD = 0.001f;

	/**
	 * Style configuration for difference display.
	 *
	 * @param positiveColor Color for positive/good changes
	 * @param negativeColor Color for negative/bad changes
	 * @param neutralColor  Color for no change
	 * @param upArrow       Arrow for positive direction
	 * @param downArrow     Arrow for negative direction
	 */
	public record DifferenceStyle(
			Formatting positiveColor,
			Formatting negativeColor,
			Formatting neutralColor,
			String upArrow,
			String downArrow
	) {
		/**
		 * Default style with standard colors and ASCII arrows.
		 */
		public static final DifferenceStyle DEFAULT = new DifferenceStyle(
				Formatting.GREEN, Formatting.RED, Formatting.WHITE, "^", "v"
		);

		/**
		 * Style with Unicode arrows.
		 */
		public static final DifferenceStyle UNICODE = new DifferenceStyle(
				Formatting.GREEN, Formatting.RED, Formatting.WHITE, "\u2191", "\u2193"
		);

		/**
		 * Style with plus/minus symbols instead of arrows.
		 */
		public static final DifferenceStyle PLUS_MINUS = new DifferenceStyle(
				Formatting.GREEN, Formatting.RED, Formatting.WHITE, "+", "-"
		);
	}

	private final DifferenceStyle defaultStyle;

	/**
	 * Creates a formatter with the default Unicode style.
	 */
	public DifferenceFormatter() {
		this(DifferenceStyle.UNICODE);
	}

	/**
	 * Creates a formatter with a custom default style.
	 *
	 * @param defaultStyle The style to use when no custom style is registered
	 */
	public DifferenceFormatter(DifferenceStyle defaultStyle) {
		this.defaultStyle = defaultStyle;
	}

	/**
	 * Registers an attribute as "inverse" (lower is better).
	 * Examples: weight, cooldown, stamina cost
	 *
	 * @param attributeId The attribute identifier
	 */
	public static void registerInverseAttribute(OpenIdentifier attributeId) {
		INVERSE_ATTRIBUTES.add(attributeId);
	}

	/**
	 * Checks if an attribute is registered as inverse.
	 *
	 * @param attributeId The attribute identifier
	 * @return true if lower values are better for this attribute
	 */
	public static boolean isInverseAttribute(OpenIdentifier attributeId) {
		return INVERSE_ATTRIBUTES.contains(attributeId);
	}

	/**
	 * Registers a custom style for a specific attribute.
	 *
	 * @param attributeId The attribute identifier
	 * @param style       The style to use
	 */
	public static void registerStyle(OpenIdentifier attributeId, DifferenceStyle style) {
		CUSTOM_STYLES.put(attributeId, style);
	}

	/**
	 * Clears all inverse attribute registrations.
	 */
	public static void clearInverseAttributes() {
		INVERSE_ATTRIBUTES.clear();
	}

	/**
	 * Clears all custom style registrations.
	 */
	public static void clearCustomStyles() {
		CUSTOM_STYLES.clear();
	}

	/**
	 * Formats a difference value with arrow and optional numeric value.
	 *
	 * @param attributeId The attribute for style lookup
	 * @param difference  The difference value (positive = increase, negative = decrease)
	 * @param showValue   Whether to show the numeric difference
	 * @return Formatted text with arrow and optional value
	 */
	public MutableText format(OpenIdentifier attributeId, float difference, boolean showValue) {
		if (isNeutral(difference)) {
			return Text.empty();
		}

		DifferenceStyle style = getStyle(attributeId);
		boolean isInverse = INVERSE_ATTRIBUTES.contains(attributeId);

		// Determine if this is effectively positive (good for the player)
		boolean effectivelyPositive = isInverse ? difference < 0 : difference > 0;

		Formatting color = effectivelyPositive ? style.positiveColor() : style.negativeColor();
		String arrow = effectivelyPositive ? style.upArrow() : style.downArrow();

		MutableText result = Text.literal(arrow).formatted(color);

		if (showValue) {
			String sign = difference > 0 ? "+" : "";
			String valueStr = TooltipTextFormatter.formatFloat(Math.abs(difference));
			result.append(Text.literal(" (" + sign + (difference > 0 ? "" : "-") + valueStr + ")").formatted(color));
		}

		return result;
	}

	/**
	 * Formats a difference with arrow only (no numeric value).
	 *
	 * @param attributeId The attribute for style lookup
	 * @param difference  The difference value
	 * @return Formatted text with arrow
	 */
	public MutableText formatArrowOnly(OpenIdentifier attributeId, float difference) {
		return format(attributeId, difference, false);
	}

	/**
	 * Gets the formatting color for a difference value.
	 *
	 * @param attributeId The attribute for style lookup
	 * @param difference  The difference value
	 * @return The appropriate color formatting
	 */
	public Formatting getColor(OpenIdentifier attributeId, float difference) {
		if (isNeutral(difference)) {
			return getStyle(attributeId).neutralColor();
		}

		DifferenceStyle style = getStyle(attributeId);
		boolean isInverse = INVERSE_ATTRIBUTES.contains(attributeId);
		boolean effectivelyPositive = isInverse ? difference < 0 : difference > 0;

		return effectivelyPositive ? style.positiveColor() : style.negativeColor();
	}

	/**
	 * Checks if a difference is effectively neutral (close to zero).
	 *
	 * @param difference The difference value
	 * @return true if the difference is within the neutral threshold
	 */
	public boolean isNeutral(float difference) {
		return Math.abs(difference) < NEUTRAL_THRESHOLD;
	}

	/**
	 * Gets the style to use for an attribute.
	 *
	 * @param attributeId The attribute identifier
	 * @return The custom style if registered, otherwise the default
	 */
	private DifferenceStyle getStyle(OpenIdentifier attributeId) {
		return CUSTOM_STYLES.getOrDefault(attributeId, defaultStyle);
	}
}
