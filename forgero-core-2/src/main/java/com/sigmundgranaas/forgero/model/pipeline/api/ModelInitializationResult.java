package com.sigmundgranaas.forgero.model.pipeline.api;

import com.sigmundgranaas.forgero.model.generation.api.ModelGenerationResult;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;

/**
 * A container for the complete result of the model data initialization process.
 * This includes the populated registry and the intermediate generation results,
 * which can be used for tasks like writing assets to disk.
 *
 * @param modelRegistry      The model registry, populated with both generated and manually-defined models.
 * @param generationResult   The result of the model generation step, containing DTOs and texture tasks.
 */
public record ModelInitializationResult(
		ModelRegistry modelRegistry,
		ModelGenerationResult generationResult
) {
}
