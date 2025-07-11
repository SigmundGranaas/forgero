package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * Base interface for all model template DTOs.
 * Ensures all templates have a 'type' field for proper deserialization.
 */
public interface ModelTemplateDTO {
	OpenIdentifier type();
}
