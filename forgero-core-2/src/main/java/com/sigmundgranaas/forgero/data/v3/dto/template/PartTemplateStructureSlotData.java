package com.sigmundgranaas.forgero.data.v3.dto.template;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

/**
 * DTO for a slot within a PartTemplate's structure.
 * Defines a requirement like a material or a sub-component.
 *
 * @param type        The type of component required (e.g., "forgero:tool_material").
 * @param count       The optional quantity required.
 * @param description Optional description for this requirement.
 */
public record PartTemplateStructureSlotData(
		OpenIdentifier type,
		@Nullable
		Integer count,
		@Nullable
		String description
) {
}
