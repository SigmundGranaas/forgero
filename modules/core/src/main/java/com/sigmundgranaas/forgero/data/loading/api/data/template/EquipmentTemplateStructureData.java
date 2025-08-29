package com.sigmundgranaas.forgero.data.loading.api.data.template;

import org.jetbrains.annotations.Nullable;
import java.util.Map;

/**
 * DTO for the 'structure' block within a ToolTemplateData.
 * Defines the required parts for tool assembly.
 * The keys of the map are the slot names (e.g., "head", "handle").
 *
 * @param id The template string for generating the ID of the composed equipment.
 * @param slots A map where keys are slot names and values are ToolTemplateSlotData.
 */
public record EquipmentTemplateStructureData(
		@Nullable String id,
		Map<String, EquipmentTemplateSlotData> slots
) {
}
