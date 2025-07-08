package com.sigmundgranaas.forgero.data.v3.dto.template;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * DTO for the 'structure' block within a PartTemplateData.
 * Defines the material requirements for crafting this part.
 *
 * @param id The template string for generating the ID of the composed part.
 * @param slots The template slots for the part.
 */
public record PartTemplateStructureData(
		@Nullable String id,
		Map<String, PartTemplateStructureSlotData> slots
) {
}
