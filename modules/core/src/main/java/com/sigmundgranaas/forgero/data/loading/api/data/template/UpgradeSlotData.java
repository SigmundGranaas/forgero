package com.sigmundgranaas.forgero.data.loading.api.data.template;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
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
 * @param context     Optional context identifier for attribute filtering (e.g., "forgero:offensive", "forgero:defensive", "forgero:utility").
 *                    When an upgrade is installed in a slot with a context, only attributes matching that context
 *                    (via tag hierarchy resolution) will be applied.
 */
public record UpgradeSlotData(
		OpenIdentifier id,
		OpenIdentifier type,
		@Nullable
		List<OpenIdentifier> tags,
		@Nullable
		Integer tier,
		@Nullable
		String description,
		@Nullable
		OpenIdentifier context
) {
}
