package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.generation.impl.ModelGeneratorImpl;

/**
 * DTO for `forgero:context_model_template` files.
 * Defines a model for a component when it is used in a specific context (e.g., an upgrade slot).
 */
public record ContextualModelTemplateDTO(
		OpenIdentifier type,
		String context,
		TargetDTO target,
		TemplateModelDTO model // Changed from ModelDTO
) implements ModelTemplateDTO, ModelGeneratorImpl.TemplateModelDataProvider {
}
