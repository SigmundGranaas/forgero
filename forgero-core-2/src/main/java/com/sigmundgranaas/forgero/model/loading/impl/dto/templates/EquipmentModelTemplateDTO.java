package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.generation.impl.ModelGeneratorImpl;

import java.util.List;

/**
 * DTO for `forgero:equipment_model_template` files.
 * Defines the model "frame" for a complete tool or piece of equipment.
 *
 * @param type   The type identifier, always "forgero:equipment_model_template".
 * @param target The target criteria for equipment, usually based on the tool type tag.
 * @param models  The model definition(s) for assembling the parts. Can be one or many.
 */
public record EquipmentModelTemplateDTO(
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
