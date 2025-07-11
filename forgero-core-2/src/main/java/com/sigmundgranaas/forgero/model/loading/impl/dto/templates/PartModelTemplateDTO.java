package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.generation.impl.ModelGeneratorImpl;

/**
 * DTO for `forgero:part_model_template` files.
 * Defines the model for a part based on its shape and iterates over materials.
 */
public record PartModelTemplateDTO(
		OpenIdentifier type,
		TargetDTO target,
		TemplateModelDTO model
) implements ModelTemplateDTO, ModelGeneratorImpl.TemplateModelDataProvider {
}
