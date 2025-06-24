package com.sigmundgranaas.forgero.core.feature.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.core.feature.api.Feature;
import com.sigmundgranaas.forgero.core.feature.api.FeatureBakedResult;

import java.util.List;
import java.util.stream.Stream;

/**
 * The expert engine for resolving Features.
 * <p>
 * Intermediate Baked Type {@code <B>}: {@link FeatureBakedResult} - An object containing pre-sorted lists of
 * statically valid features (separated into static and dynamic).
 * <p>
 * Final Result Type {@code <R>}: {@code List<Feature>} - The final list of all features that are active
 * in a given {@link DynamicContext}.
 */
public class FeatureEngine implements DataTypeEngine<FeatureBakedResult, List<Feature>> {
	public static final ResolutionKey<List<Feature>> KEY = new ResolutionKey<>(new OpenIdentifier("forgero", "features"));

	@Override
	public ResolutionKey<List<Feature>> key() {
		return KEY;
	}

	@Override
	public FeatureBakedResult bake(Stream<Component> components) {
		List<Component> componentList = components.toList();
		Component root = componentList.get(0);

		// 1. Collect all features and evaluate their STATIC conditions
		List<Feature> staticallyValidFeatures = componentList.stream()
				.flatMap(component -> component.getProperties().stream()
						.filter(Feature.class::isInstance)
						.map(Feature.class::cast)
						.filter(feature -> {
							ResolutionContext resCtx = new ResolutionContext(component, root);
							return resCtx.test(feature.condition().staticConditions());
						}))
				.toList();

		// 2. Separate into static and dynamic features for optimized application later.
		// A feature is fully static if it has NO dynamic conditions.
		List<Feature> staticFeatures = staticallyValidFeatures.stream()
				.filter(feat -> feat.condition().dynamicConditions().isEmpty())
				.toList();

		// A feature is dynamic if it has one or more dynamic conditions.
		List<Feature> dynamicFeatures = staticallyValidFeatures.stream()
				.filter(feat -> !feat.condition().dynamicConditions().isEmpty())
				.toList();

		// 3. Return the lightweight, cachable result containing the pre-filtered lists.
		return new FeatureBakedResult(staticFeatures, dynamicFeatures);
	}

	@Override
	public List<Feature> apply(FeatureBakedResult baked, DynamicContext context) {
		// The FeatureBakedResult record contains the logic for this final step.
		return baked.getActiveFeatures(context);
	}
}
