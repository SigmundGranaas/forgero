package com.sigmundgranaas.forgero.model.api.item;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.ModelLayer;
import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import com.sigmundgranaas.forgero.model.api.ModelSlot;

import java.util.List;
import java.util.Optional;

/**

 A model composed of multiple layers and slots for other components.

 This is the primary model type for building complex tools and parts.

 @param identifier The unique identifier for this model.

 @param layers The visual layers of this model itself.

 @param slots The slots where other components can be attached.

 @param target The optional target component ID this model is for (used for contextual registration).

 @param context The optional context this model belongs to (used for contextual registration).

 @param parent The optional parent model to inherit transformations from.

 @param display The optional display block for custom transformations.
 */
public record CompositeModel(OpenIdentifier identifier, List<ModelLayer> layers, List<ModelSlot> slots, Optional<OpenIdentifier> target, Optional<String> context, Optional<OpenIdentifier> parent, Optional<JsonElement> display) implements Model {

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
	public Model apply(ModelResolutionContext context) {
		// Composite models don't change themselves based on context, they provide it.
		return this;
	}
}
