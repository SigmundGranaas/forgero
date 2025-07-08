package com.sigmundgranaas.forgero.data.dto.template;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

/**
 * DTO for a slot definition within a ToolTemplateData's 'structure'.
 *
 * @param type            The type tag of component accepted by this slot (e.g., "forgero:pickaxe_head").
 * @param defaultTag      An optional tag that identifies a pool of "default" parts for this slot.
 * @param defaultComponent An optional, concrete ID of a single default part for this slot.
 */
public record EquipmentTemplateSlotData(
		OpenIdentifier type,
		@Nullable
		OpenIdentifier defaultTag,
		@Nullable
		OpenIdentifier defaultComponent
) {
}
