package com.sigmundgranaas.forgero.data.v3.dto;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier; // Import OpenIdentifier
import org.jetbrains.annotations.Nullable;

/**
 * DTO for the 'material' block within a PartTemplateData's 'structure'.
 *
 * @param type        The type of material required (e.g., "forgero:tool_material").
 * @param count       The quantity of the material required.
 * @param description Optional description for this material requirement.
 */
public record PartTemplateStructureMaterialData(
		OpenIdentifier type, // Changed from String
		int count,
		@Nullable
		String description
) {
}
