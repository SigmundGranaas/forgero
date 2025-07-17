package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.generation.impl.ModelGeneratorImpl;

import java.util.List;

/**
 * DTO for `forgero:part_model_template` files.
 * Defines the model for a part based on its shape and iterates over materials.
 */
public record PartModelTemplateDTO(
		OpenIdentifier type,
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
