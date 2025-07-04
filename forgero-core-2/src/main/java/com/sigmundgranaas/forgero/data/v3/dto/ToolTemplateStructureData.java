package com.sigmundgranaas.forgero.data.v3.dto;

import java.util.Map;

/**
 * DTO for the 'structure' block within a ToolTemplateData.
 * Defines the required parts for tool assembly.
 * The keys of the map are the slot names (e.g., "head", "handle").
 *
 * @param slots A map where keys are slot names and values are ToolTemplateSlotData.
 */
public record ToolTemplateStructureData(
		Map<String, ToolTemplateSlotData> slots
) {
}
