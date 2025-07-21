package com.sigmundgranaas.forgero.model.api;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Optional;

public sealed interface Model permits CompositeModel, TextureModel, EmptyModel {
	OpenIdentifier getIdentifier();

	/**
	 * Returns the target component ID this model is designed for, primarily used for contextual models.
	 *
	 * @return An optional containing the target's OpenIdentifier.
	 */
	Optional<OpenIdentifier> getTarget();

	/**
	 * Returns the context string (e.g., "pickaxe_head_reinforcement") this model belongs to.
	 *
	 * @return An optional containing the context string.
	 */
	Optional<String> getContext();

	/**
	 * Gets the identifier of a parent model whose display properties should be inherited.
	 *
	 * @return An optional containing the parent model's identifier.
	 */
	Optional<OpenIdentifier> getParent();

	/**
	 * Gets the raw JSON display block, which specifies per-viewport transformations.
	 * This overrides any properties inherited from a parent.
	 *
	 * @return An optional containing the display properties as a JsonElement.
	 */
	Optional<JsonElement> getDisplay();

	/**
	 * Applies the given context to the model, resolving any variants or predicates.
	 * This method allows a model to return a context-specific version of itself.
	 * For example, a `TextureModel` might return a variant with a different texture based on state.
	 *
	 * @param context The current resolution context.
	 * @return A new, context-specific model instance, or the same instance if no changes are needed.
	 */
	Model apply(ModelResolutionContext context);
}
