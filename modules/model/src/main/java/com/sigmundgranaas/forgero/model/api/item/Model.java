package com.sigmundgranaas.forgero.model.api.item;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.Contextual;
import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import com.sigmundgranaas.forgero.model.api.MountPoint;

import java.util.List;
import java.util.Optional;

/**
 * Base interface for item models using composition.
 * Extends Identifiable and Contextual to compose capabilities.
 */
public sealed interface Model extends Identifiable, Contextual permits CompositeModel, TextureModel, EmptyModel {
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

	// Bridge methods for Contextual interface (delegates to getTarget/getContext)
	@Override
	default Optional<OpenIdentifier> target() {
		return getTarget();
	}

	@Override
	default Optional<String> context() {
		return getContext();
	}

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
	 * Gets the list of mount points defined for this model.
	 *
	 * @return A list of MountPoint objects.
	 */
	List<MountPoint> getMountPoints();

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
