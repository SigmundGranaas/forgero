package com.sigmundgranaas.forgero.core.feature.api;

import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import java.util.List;
import java.util.stream.Stream;

/**
 * The specific intermediate baked result for features, used by {@link com.sigmundgranaas.forgero.core.feature.impl.FeatureEngine}.
 * This object is lightweight and designed for caching. It holds pre-filtered
 * lists of features that have passed their static conditions, separating them into
 * those that are fully static and those that still have dynamic conditions to evaluate.
 * This pre-sorting makes the final "apply" phase very fast.
 *
 * @param staticFeatures  A list of features with no dynamic conditions. These are always active.
 * @param dynamicFeatures A list of features that have at least one dynamic condition and require
 *                        further filtering against a {@link DynamicContext}.
 */
public record FeatureBakedResult(List<Feature> staticFeatures, List<Feature> dynamicFeatures) {

	/**
	 * Computes the final list of active features by filtering the dynamic features against
	 * the provided context and combining them with the already-active static features.
	 *
	 * @param context The dynamic context for the calculation.
	 * @return The final list of active features for this context.
	 */
	public List<Feature> getActiveFeatures(DynamicContext context) {
		// Filter the small list of dynamic features.
		List<Feature> activeDynamicFeatures = dynamicFeatures.stream()
				.filter(feature -> feature.condition().dynamicConditions().stream()
						.allMatch(cond -> cond.test(context)))
				.toList();

		// If no dynamic features became active, we can return the cached static list directly.
		if (activeDynamicFeatures.isEmpty()) {
			return staticFeatures;
		}

		// Otherwise, combine the two lists.
		return Stream.concat(staticFeatures.stream(), activeDynamicFeatures.stream()).toList();
	}
}
