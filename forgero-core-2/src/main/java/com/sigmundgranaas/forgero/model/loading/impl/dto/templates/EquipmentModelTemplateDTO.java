package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.generation.impl.ModelGeneratorImpl;

/**
 * DTO for `forgero:equipment_model_template` files.
 * Defines the model "frame" for a complete tool or piece of equipment.
 *
 * @param type   The type identifier, always "forgero:equipment_model_template".
 * @param target The target criteria for equipment, usually based on the tool type tag.
 * @param model  The model definition for assembling the parts.
 */
public record EquipmentModelTemplateDTO(
		OpenIdentifier type,
		TargetDTO target,
		TemplateModelDTO model
) implements ModelTemplateDTO, ModelGeneratorImpl.TemplateModelDataProvider {
}
