package com.sigmundgranaas.forgero.data.v3.dto;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier; // Import OpenIdentifier
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * DTO for an upgrade slot definition within a part template or tool template.
 *
 * @param id          A unique identifier for this specific slot instance.
 * @param type        The type of upgrade material/component accepted (e.g., "forgero:upgrade_material", "forgero:binding").
 * @param tags        Optional list of tags that an accepted upgrade must possess.
 * @param tier        Optional tier requirement for the upgrade.
 * @param description Optional translatable description key for the slot.
 */
public record UpgradeSlotData(
		OpenIdentifier id, // Changed from String
		OpenIdentifier type, // Changed from String
		@Nullable
		List<OpenIdentifier> tags, // Changed from List<String>
		@Nullable
		Integer tier,
		@Nullable
		String description
) {
}
