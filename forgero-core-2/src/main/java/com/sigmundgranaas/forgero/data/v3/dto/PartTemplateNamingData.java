package com.sigmundgranaas.forgero.data.v3.dto;

/**
 * DTO for the 'naming' block within a PartTemplateData.
 * Defines the naming pattern for generated parts from this template.
 *
 * @param pattern The naming pattern string (e.g., "{material_name} Mandrill Pickaxe Head").
 */
public record PartTemplateNamingData(
		String pattern
) {
}
