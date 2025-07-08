package com.sigmundgranaas.forgero.core.attribute.impl.computation;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.operator.Operator;

import java.util.List;
import java.util.Locale;

/**
 * A utility class to visualize and debug the step-by-step computation of an attribute chain.
 * It generates a string that details how each attribute's operation affects the current value,
 * respecting groups and operator precedence.
 */
public class CalculationVisualizer {

	private final List<? extends Attribute> sortedAttributes;

	/**
	 * Constructs a CalculationVisualizer.
	 * The attributes are sorted internally based on their group and operator order,
	 * mirroring the logic in {@link ComputationChain}.
	 *
	 * @param attributes The list of attributes to visualize.
	 */
	public CalculationVisualizer(List<? extends Attribute> attributes) {
		this.sortedAttributes = attributes;
	}

	/**
	 * Generates a step-by-step debug string for the computation chain.
	 *
	 * @param initialBaseValue The starting value for the computation.
	 * @return A formatted string detailing the calculation process.
	 */
	public String visualize(float initialBaseValue) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(Locale.US, "--- Calculation Chain Debug (%d attributes) ---\n", sortedAttributes.size()));
		sb.append(String.format(Locale.US, "Initial Base Value: %.2f\n", initialBaseValue));
		sb.append("------------------------------------------\n");

		float currentValue = initialBaseValue;
		int currentGroup = -1; // To track group changes

		for (Attribute attribute : sortedAttributes) {
			// Check for group change
			if (attribute.group() != currentGroup) {
				currentGroup = attribute.group();
				sb.append(String.format(Locale.US, "\n--- Entering Group %d (Current Value entering group: %.2f) ---\n", currentGroup, currentValue));
			}

			Operator operator = attribute.operator();
			float attributeValue = attribute.value();
			float newValue = operator.apply(currentValue, attributeValue);

			sb.append(String.format(Locale.US, "  Attribute: %s (Group %d, OpOrder %d)\n",
					attribute.type().toString(), attribute.group(), operator.order()));
			sb.append(String.format(Locale.US, "    Operation: %s (on %.2f and %.2f)\n",
					operator.getClass().getSimpleName().replace("Operator", ""), currentValue, attributeValue));
			sb.append(String.format(Locale.US, "    Result: %.2f\n", newValue));
			currentValue = newValue;
		}

		sb.append("\n------------------------------------------\n");
		sb.append(String.format(Locale.US, "Final Result: %.2f\n", currentValue));
		sb.append("------------------------------------------\n");

		return sb.toString();
	}
}
