package com.sigmundgranaas.forgero.data.processing.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * Stage 1 Output: A raw, unprocessed definition from a single file,
 * associating the canonical ID with the raw DTO object.
 *
 * @param id   The canonical ID of the definition (e.g., forgero:iron).
 * @param data The raw DTO object parsed from JSON (e.g., MaterialData, PartTemplateData).
 */
public record RawDefinition(OpenIdentifier id, Object data) {
}
