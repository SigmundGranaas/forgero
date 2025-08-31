package com.sigmundgranaas.forgero.model.generation.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;

import java.util.List;
import java.util.Map;

/**
 * A container for the results of the model generation process.
 *
 * @param generatedModels        A map of all generated item model definitions, keyed by their intended identifier.
 * @param generatedArmorModels   A map of all generated armor model definitions, keyed by their intended identifier.
 * @param textureGenerationTasks A list of all tasks required to generate the corresponding textures.
 */
public record ModelGenerationResult(
		Map<OpenIdentifier, ModelDTO> generatedModels,
		Map<OpenIdentifier, ArmorModelDTO> generatedArmorModels,
		List<TextureGenerationTask> textureGenerationTasks
) {
}
