package com.sigmundgranaas.forgero.model.api;

import java.util.List;
import java.util.Optional;

/**
 * Represents a single visual layer within a CompositeModel.
 * It can have a default texture and a list of variants that can override the default
 * based on contextual predicates.
 *
 * @param texture  The default texture identifier for this layer.
 * @param order    The rendering order for this layer.
 * @param variants A list of alternative textures that can be chosen based on predicates.
 * @param offset   An optional offset for positioning this layer.
 */
public record ModelLayer(String texture, int order, List<ModelVariant> variants, Optional<Offset> offset) {
	public Optional<ModelVariant> getActiveVariant(ModelResolutionContext context) {
		return variants.stream()
				.filter(variant -> variant.predicate().stream().allMatch(p -> p.test(context)))
				.findFirst();
	}
}
