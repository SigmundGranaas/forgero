package com.sigmundgranaas.forgero.model.api.item;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import com.sigmundgranaas.forgero.model.api.ModelVariant;
import com.sigmundgranaas.forgero.model.api.MountPoint;
import com.sigmundgranaas.forgero.model.api.Offset;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A simple model that represents a single texture.
 * It can have variants that change the texture based on contextual predicates.
 *
 * @param identifier  The unique identifier for this model.
 * @param texture     The default texture identifier.
 * @param variants    A list of alternative textures that can be chosen based on predicates.
 * @param offset      An optional offset for positioning this texture.
 * @param mountPoints The named coordinates on this model for alignment.
 * @param target      The optional target component ID this model is for (used for contextual registration).
 * @param context     The optional context this model belongs to (used for contextual registration).
 * @param parent      The optional parent model to inherit transformations from.
 * @param display     The optional display block for custom transformations.
 */
public record TextureModel(OpenIdentifier identifier, String texture, List<ModelVariant> variants, Optional<Offset> offset, List<MountPoint> mountPoints, Optional<OpenIdentifier> target, Optional<String> context, Optional<OpenIdentifier> parent, Optional<JsonElement> display) implements Model {

	@Override
	public OpenIdentifier getIdentifier() {
		return identifier;
	}

	@Override
	public Optional<OpenIdentifier> getTarget() {
		return target;
	}

	@Override
	public Optional<String> getContext() {
		return context;
	}

	@Override
	public Optional<OpenIdentifier> getParent() {
		return parent;
	}

	@Override
	public Optional<JsonElement> getDisplay() {
		return display;
	}

	@Override
	public List<MountPoint> getMountPoints() {
		return mountPoints != null ? mountPoints : Collections.emptyList();
	}

	public Optional<ModelVariant> getActiveVariant(ModelResolutionContext context) {
		return variants.stream()
				.filter(variant -> variant.predicate().stream().allMatch(p -> p.test(context)))
				.findFirst();
	}

	@Override
	public Model apply(ModelResolutionContext context) {
		// A variant can override the ENTIRE model. Check for this case.
		return variants.stream()
				.filter(variant -> variant.model().isPresent() && variant.predicate().stream().allMatch(p -> p.test(context)))
				.findFirst()
				.flatMap(ModelVariant::model)
				.filter(Model.class::isInstance)
				.map(Model.class::cast)
				.orElse(this);
	}
}
