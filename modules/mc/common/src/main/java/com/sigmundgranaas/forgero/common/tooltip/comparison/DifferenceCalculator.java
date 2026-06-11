package com.sigmundgranaas.forgero.common.tooltip.comparison;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.Optional;

/**
 * Calculates differences between current and baseline attribute values.
 * <p>
 * Uses the comparison context to determine what baseline to compare against,
 * then computes the difference for display in tooltips.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * DifferenceCalculator calculator = new DifferenceCalculator(resolver);
 *
 * // Calculate difference for attack damage
 * Optional<AttributeDifference> diff = calculator.calculate(
 *     currentComponent,
 *     DefaultAttributes.ATTACK_DAMAGE,
 *     ComparisonContext.strippedItem(stripped)
 * );
 *
 * diff.ifPresent(d -> {
 *     if (d.isPositive()) {
 *         // Show green arrow
 *     }
 * });
 * }</pre>
 */
public class DifferenceCalculator {

	/**
	 * Creates a calculator that uses AttributeEngine static methods for resolution.
	 */
	public DifferenceCalculator() {
	}

	/**
	 * Calculates the difference for an attribute based on the comparison context.
	 *
	 * @param current           The current component
	 * @param attributeId       The attribute to calculate difference for
	 * @param comparisonContext The context defining the baseline
	 * @return The difference if a comparison is applicable, empty otherwise
	 */
	public Optional<AttributeDifference> calculate(
			Component current,
			OpenIdentifier attributeId,
			ComparisonContext comparisonContext
	) {
		float currentValue = AttributeEngine.getAttribute(current, attributeId);

		Optional<Float> baselineValue = getBaselineValue(attributeId, comparisonContext);

		return baselineValue.map(baseline -> {
			float diff = currentValue - baseline;
			return new AttributeDifference(attributeId, currentValue, baseline, diff);
		});
	}

	/**
	 * Calculates differences for all attributes in a result.
	 *
	 * @param currentResult     The already-resolved attributes for the current component
	 * @param attributeId       The attribute to calculate difference for
	 * @param comparisonContext The context defining the baseline
	 * @return The difference if a comparison is applicable, empty otherwise
	 */
	public Optional<AttributeDifference> calculateFromResult(
			AttributeQueryResult currentResult,
			OpenIdentifier attributeId,
			ComparisonContext comparisonContext
	) {
		float currentValue = currentResult.getValue(attributeId);

		Optional<Float> baselineValue = getBaselineValue(attributeId, comparisonContext);

		return baselineValue.map(baseline -> {
			float diff = currentValue - baseline;
			return new AttributeDifference(attributeId, currentValue, baseline, diff);
		});
	}

	/**
	 * Gets the baseline value for an attribute based on the comparison context.
	 */
	private Optional<Float> getBaselineValue(
			OpenIdentifier attributeId,
			ComparisonContext context
	) {
		if (context instanceof ComparisonContext.None) {
			return Optional.empty();
		}

		if (context instanceof ComparisonContext.StrippedItem stripped) {
			float value = AttributeEngine.getAttribute(stripped.strippedComponent(), attributeId);
			return Optional.of(value);
		}

		if (context instanceof ComparisonContext.VsComponent vs) {
			float value = AttributeEngine.getAttribute(vs.baselineComponent(), attributeId);
			return Optional.of(value);
		}

		if (context instanceof ComparisonContext.WithoutUpgrade without) {
			float value = AttributeEngine.getAttribute(without.baseWithoutUpgrade(), attributeId);
			return Optional.of(value);
		}

		if (context instanceof ComparisonContext.Custom custom) {
			return custom.provider().getBaselineValue(attributeId);
		}

		return Optional.empty();
	}

	/**
	 * Result of a difference calculation.
	 *
	 * @param attributeId   The attribute this difference is for
	 * @param currentValue  The current value of the attribute
	 * @param baselineValue The baseline value being compared against
	 * @param difference    The difference (current - baseline)
	 */
	public record AttributeDifference(
			OpenIdentifier attributeId,
			float currentValue,
			float baselineValue,
			float difference
	) {
		private static final float THRESHOLD = 0.001f;

		/**
		 * Checks if the difference represents an increase.
		 */
		public boolean isPositive() {
			return difference > THRESHOLD;
		}

		/**
		 * Checks if the difference represents a decrease.
		 */
		public boolean isNegative() {
			return difference < -THRESHOLD;
		}

		/**
		 * Checks if there is effectively no difference.
		 */
		public boolean isNeutral() {
			return !isPositive() && !isNegative();
		}

		/**
		 * Gets the absolute difference value.
		 */
		public float absoluteDifference() {
			return Math.abs(difference);
		}

		/**
		 * Gets the percentage change from baseline.
		 *
		 * @return The percentage change, or 0 if baseline is 0
		 */
		public float percentageChange() {
			if (Math.abs(baselineValue) < THRESHOLD) {
				return 0f;
			}
			return (difference / baselineValue) * 100f;
		}
	}
}
