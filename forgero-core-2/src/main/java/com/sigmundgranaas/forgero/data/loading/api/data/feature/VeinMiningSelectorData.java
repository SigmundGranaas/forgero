package com.sigmundgranaas.forgero.data.loading.api.data.feature;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier; // Import OpenIdentifier

/**
 * DTO for the selector block within a vein mining feature.
 * Currently only supports "forgero:radius" type.
 *
 * @param type   The type of selector (e.g., "forgero:radius").
 * @param radius The radius for vein mining.
 * @param tag    The tag to filter for vein mining ores.
 */
public record VeinMiningSelectorData(
		OpenIdentifier type, // Changed from String
		int radius,
		OpenIdentifier tag // Changed from String
) {
}
