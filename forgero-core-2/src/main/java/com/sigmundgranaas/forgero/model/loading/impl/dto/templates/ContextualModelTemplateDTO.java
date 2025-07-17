package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.generation.impl.ModelGeneratorImpl;

import java.util.List;

/**
 * DTO for `forgero:context_model_template` files.
 * Defines a model for a component when it is used in a specific context (e.g., an upgrade slot).
 */
public record ContextualModelTemplateDTO(
		OpenIdentifier type,
		String context,
		TargetDTO target,
		List<TemplateModelDTO> models
) implements ModelTemplateDTO, ModelGeneratorImpl.TemplateModelDataProvider {
	public TemplateModelDTO model() {
		if (models == null || models.isEmpty()) {
			throw new IllegalStateException("EquipmentModelTemplateDTO has no models defined.");
		}
		return models.get(0);
	}
}
