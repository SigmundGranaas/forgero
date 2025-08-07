package com.sigmundgranaas.forgero.data.loading.api.data.host;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

/**
 * DTO specifying how to create a new host platform item.
 *
 * @param id          The unique ID for the new item (e.g., "forgero:iron_pickaxe_head_item").
 * @param itemClass   An identifier for the Java class to instantiate (e.g., "forgero:part_item").
 *                    This is resolved to a real class by a platform-specific service.
 * @param itemGroup  An optional item group/creative tab ID for the new item.
 */
public record CreateData(
		OpenIdentifier id,
		String itemClass,
		@Nullable String itemGroup
) {
}
