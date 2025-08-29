package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.generation.impl.TemplateDataProvider;

import java.util.List;

/**
 * DTO for `forgero:armor_model_template` files.
 * Defines the models for a piece of armor.
 *
 * @param type   The type identifier, always "forgero:armor_model_template".
 * @param target The target criteria for armor, usually based on an armor type tag.
 * @param models The model definition(s) for the armor piece.
 */
public record ArmorModelTemplateDTO(
		OpenIdentifier type,
		TargetDTO target,
		List<TemplateArmorModelDTO> models
) implements ModelTemplateDTO, TemplateDataProvider<TemplateArmorModelDTO> {
}
