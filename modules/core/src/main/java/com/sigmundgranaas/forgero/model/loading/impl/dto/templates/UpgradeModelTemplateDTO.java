package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.generation.impl.TemplateDataProvider;

import java.util.List;

/**
 * DTO for `forgero:upgrade_model_template` files.
 * Defines a model for a component when it is used in a specific context (e.g., an upgrade slot).
 */
public record UpgradeModelTemplateDTO(
		OpenIdentifier type,
		String context,
		TargetDTO target,
		List<TemplateModelDTO> models
) implements ModelTemplateDTO, TemplateDataProvider<TemplateModelDTO> {
}
