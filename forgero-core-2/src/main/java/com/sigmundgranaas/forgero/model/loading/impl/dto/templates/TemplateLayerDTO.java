package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

/**
 * DTO for a layer within a model template.
 * This is distinct from LayerDTO as it contains a generation block.
 */
public record TemplateLayerDTO(
		int order,
		TemplateTexturesDTO textures
) {
}
