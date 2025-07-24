package com.sigmundgranaas.forgero.model.pipeline.api;

import com.sigmundgranaas.forgero.model.generation.api.ModelGenerationResult;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;

/**
 * A container for the complete result of the model data initialization process.
 * This includes the populated registries and the intermediate generation results,
 * which can be used for tasks like writing assets to disk.
 *
 * @param itemModelRegistry  The item model registry, populated with generated models.
 * @param armorModelRegistry The armor model registry, populated with generated models.
 * @param generationResult   The result of the model generation step, containing DTOs and texture tasks.
 */
public record ModelInitializationResult(
		ItemModelRegistry itemModelRegistry,
		ArmorModelRegistry armorModelRegistry,
		ModelGenerationResult generationResult
) {
}
