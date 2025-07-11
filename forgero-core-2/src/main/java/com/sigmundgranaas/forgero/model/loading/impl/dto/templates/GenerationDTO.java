package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * DTO for the "generation" block within a model template, which describes how
 * to generate a texture.
 *
 * @param type     The type of generation, e.g., "forgero:palette_retexture".
 * @param template The greyscale texture template to use. This path supports placeholders.
 * @param palette  The color palette to apply. This path supports placeholders.
 * @param output   The output path for the generated texture. This path supports placeholders.
 */
public record GenerationDTO(
		OpenIdentifier type,
		String template,
		String palette,
		String output
) {
}
