package com.sigmundgranaas.forgero.model.util;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.Contextual;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Modular utility for finding contextual models using composition.
 *
 * This utility works with ANY model that implements the Contextual capability,
 * enabling context-based filtering without inheritance.
 */
public class ContextualFinder {
	/**
	 * Finds all models targeting a specific component.
	 *
	 * @param models The collection of models to search
	 * @param target The target component ID
	 * @param <M>    The model type (must be Identifiable and Contextual)
	 * @return A list of models that target the specified component
	 */
	public static <M extends Identifiable & Contextual> List<M> findByTarget(Collection<M> models, OpenIdentifier target) {
		return models.stream()
				.filter(model -> model.target().isPresent())
				.filter(model -> model.target().get().equals(target))
				.toList();
	}

	/**
	 * Finds all models for a specific context.
	 *
	 * @param models  The collection of models to search
	 * @param context The context string
	 * @param <M>     The model type (must be Identifiable and Contextual)
	 * @return A list of models in the specified context
	 */
	public static <M extends Identifiable & Contextual> List<M> findByContext(Collection<M> models, String context) {
		return models.stream()
				.filter(model -> model.context().isPresent())
				.filter(model -> model.context().get().equals(context))
				.toList();
	}

	/**
	 * Finds a model by target and context.
	 *
	 * @param models  The collection of models to search
	 * @param target  The target component ID
	 * @param context The context string
	 * @param <M>     The model type (must be Identifiable and Contextual)
	 * @return An optional containing the first matching model
	 */
	public static <M extends Identifiable & Contextual> Optional<M> findByTargetAndContext(
			Collection<M> models,
			OpenIdentifier target,
			String context
	) {
		return models.stream()
				.filter(model -> model.target().isPresent() && model.target().get().equals(target))
				.filter(model -> model.context().isPresent() && model.context().get().equals(context))
				.findFirst();
	}

	/**
	 * Checks if a model is contextual (has both target and context).
	 */
	public static <M extends Contextual> boolean isContextual(M model) {
		return model.target().isPresent() && model.context().isPresent();
	}

	/**
	 * Gets all non-contextual models (models without target/context).
	 */
	public static <M extends Identifiable & Contextual> List<M> getNonContextual(Collection<M> models) {
		return models.stream()
				.filter(model -> !isContextual(model))
				.toList();
	}
}
