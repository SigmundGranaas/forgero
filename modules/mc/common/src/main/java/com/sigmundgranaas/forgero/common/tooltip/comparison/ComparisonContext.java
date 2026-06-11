package com.sigmundgranaas.forgero.common.tooltip.comparison;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.Optional;

/**
 * Defines the comparison baseline for tooltip attribute display.
 * <p>
 * Different UI contexts require different comparison modes:
 * <ul>
 *   <li>In inventory: compare against stripped item (without upgrades)</li>
 *   <li>In upgrade UI: compare against item without the hovered upgrade</li>
 *   <li>In crafting: compare against the current item or no comparison</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // No comparison - show raw values only
 * ComparisonContext context = ComparisonContext.none();
 *
 * // Compare against item without upgrades
 * ComparisonContext context = ComparisonContext.strippedItem(strippedComponent);
 *
 * // Compare against a specific baseline
 * ComparisonContext context = ComparisonContext.vsComponent(baselineComponent);
 * }</pre>
 */
public sealed interface ComparisonContext {

	/**
	 * No comparison - show raw values only.
	 */
	record None() implements ComparisonContext {
	}

	/**
	 * Compare against the item stripped of all upgrades.
	 * Shows how upgrades affect the item's stats.
	 *
	 * @param strippedComponent The component with all upgrades removed
	 */
	record StrippedItem(Component strippedComponent) implements ComparisonContext {
	}

	/**
	 * Compare against a specific component (e.g., the item before an upgrade is applied).
	 *
	 * @param baselineComponent The component to compare against
	 */
	record VsComponent(Component baselineComponent) implements ComparisonContext {
	}

	/**
	 * Compare against the item without a specific upgrade slot filled.
	 * Useful for showing the effect of a single upgrade.
	 *
	 * @param slotId           The slot that would be empty in the baseline
	 * @param baseWithoutUpgrade The component with that slot empty
	 */
	record WithoutUpgrade(OpenIdentifier slotId, Component baseWithoutUpgrade) implements ComparisonContext {
	}

	/**
	 * Custom comparison with a provider function.
	 * Allows arbitrary comparison logic.
	 *
	 * @param provider A function that returns baseline values for attributes
	 */
	record Custom(ComparisonProvider provider) implements ComparisonContext {
	}

	/**
	 * Provider interface for custom comparison logic.
	 */
	@FunctionalInterface
	interface ComparisonProvider {
		/**
		 * Gets the baseline value for an attribute.
		 *
		 * @param attributeId The attribute to get the baseline for
		 * @return The baseline value, or empty if no comparison should be shown
		 */
		Optional<Float> getBaselineValue(OpenIdentifier attributeId);
	}

	// Factory methods

	/**
	 * Creates a context with no comparison.
	 */
	static ComparisonContext none() {
		return new None();
	}

	/**
	 * Creates a context comparing against a stripped item.
	 *
	 * @param stripped The component with upgrades removed
	 */
	static ComparisonContext strippedItem(Component stripped) {
		return new StrippedItem(stripped);
	}

	/**
	 * Creates a context comparing against a specific component.
	 *
	 * @param baseline The component to compare against
	 */
	static ComparisonContext vsComponent(Component baseline) {
		return new VsComponent(baseline);
	}

	/**
	 * Creates a context comparing against the item without a specific upgrade.
	 *
	 * @param slotId  The slot ID of the upgrade
	 * @param base    The component without that upgrade
	 */
	static ComparisonContext withoutUpgrade(OpenIdentifier slotId, Component base) {
		return new WithoutUpgrade(slotId, base);
	}

	/**
	 * Creates a context with custom comparison logic.
	 *
	 * @param provider The provider for baseline values
	 */
	static ComparisonContext custom(ComparisonProvider provider) {
		return new Custom(provider);
	}
}
