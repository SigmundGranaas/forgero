package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * DTO for the "target" block in model templates, specifying which components
 * the template should apply to.
 *
 * @param tag The tag that components must have to be targeted by this template.
 */
public record TargetDTO(OpenIdentifier tag) {
}
