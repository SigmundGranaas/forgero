package com.sigmundgranaas.forgero.common.tooltip.display;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Optional;

/**
 * A declarative record that defines how a specific Forgero attribute should be displayed in a tooltip.
 * This class is immutable and serves as a configuration for rendering attributes.
 *
 * @param id             The unique identifier of the attribute.
 * @param translationKey The root key for translation (e.g., "attribute.forgero.attack_damage").
 * @param style          The display style (e.g., ADDITIVE, PERCENTAGE, BASE_VALUE).
 * @param defaultValue   The value considered "default" or "vanilla-equivalent". Tooltips won't show the attribute if its value matches this.
 * @param baseValue      A value to add to the attribute's raw value before scaling (e.g., -4.0 for attack speed).
 * @param scale          A multiplier for the value after adding the base (e.g., 1.0 for most, 100.0 for percentages).
 */
public record AttributeDisplayData(
		OpenIdentifier id,
		String translationKey,
		Style style,
		Optional<Float> defaultValue,
		float baseValue,
		float scale) {

	public enum Style {
		ADDITIVE,
		PERCENTAGE,
		BASE_VALUE
	}

	// Convenience constructor for attributes with a default value but no base/scale adjustments.
	public AttributeDisplayData(OpenIdentifier id, String translationKey, Style style, float defaultValue) {
		this(id, translationKey, style, Optional.of(defaultValue), 0.0f, 1.0f);
	}

	// Convenience constructor for attributes with no default value and no base/scale adjustments.
	public AttributeDisplayData(OpenIdentifier id, String translationKey, Style style) {
		this(id, translationKey, style, Optional.empty(), 0.0f, 1.0f);
	}

	/**
	 * Calculates the final display value based on the attribute's raw value and this definition.
	 *
	 * @param rawValue The value from the AttributeQueryResult.
	 * @return The calculated value ready for formatting.
	 */
	public float getDisplayValue(float rawValue) {
		return (rawValue + baseValue) * scale;
	}

	/**
	 * Checks if the given raw value is effectively the default value for this attribute.
	 *
	 * @param rawValue The raw value from the attribute query.
	 * @return true if the value is the default and should be hidden, false otherwise.
	 */
	public boolean isDefault(float rawValue) {
		return defaultValue.map(def -> Math.abs(rawValue - def) < 0.001f).orElse(false);
	}
}
