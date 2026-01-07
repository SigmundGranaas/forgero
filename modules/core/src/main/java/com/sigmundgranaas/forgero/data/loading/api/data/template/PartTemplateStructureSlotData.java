package com.sigmundgranaas.forgero.data.loading.api.data.template;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

/**
 * DTO for a slot within a PartTemplate's structure.
 * Defines a requirement like a material or a sub-component.
 *
 * @param type        The type of component required (e.g., "forgero:materials/roles/tool_material").
 * @param defaultTag  Optional tag to filter default components for generation.
 * @param count       The optional quantity required.
 * @param description Optional description for this requirement.
 */
public record PartTemplateStructureSlotData(
		OpenIdentifier type,
		@Nullable
		OpenIdentifier defaultTag,
		@Nullable
		Integer count,
		@Nullable
		String description
) {
}
