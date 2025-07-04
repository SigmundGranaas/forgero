package com.sigmundgranaas.forgero.data.v3.dto;

/**
 * DTO for the 'structure' block within a PartTemplateData.
 * Defines the material requirements for crafting this part.
 *
 * @param material The primary material requirement for the part.
 */
public record PartTemplateStructureData(
		PartTemplateStructureMaterialData material
) {
}
