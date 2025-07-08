package com.sigmundgranaas.forgero.data.v3.dto.template;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier; // Import OpenIdentifier
import org.jetbrains.annotations.Nullable;

/**
 * DTO for a slot definition within a ToolTemplateData's 'structure'.
 *
 * @param type    The type tag of component accepted by this slot (e.g., "forgero:pickaxe_head").
 * @param defaultComponent The ID of a default component to use for this slot during combinatorial generation.
 */
public record EquipmentTemplateSlotData(
		OpenIdentifier type, // Changed from String
		@Nullable
		OpenIdentifier defaultComponent // Changed from String
) {
}
