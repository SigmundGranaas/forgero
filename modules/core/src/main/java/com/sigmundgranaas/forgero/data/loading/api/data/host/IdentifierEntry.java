package com.sigmundgranaas.forgero.data.loading.api.data.host;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * DTO for a single entry used to find an existing host platform item.
 *
 * @param type The type of identifier, either "item" or "tag".
 * @param id   The identifier to search for (e.g., "minecraft:iron_ingot" or "c:iron_ingots").
 */
public record IdentifierEntry(String type, OpenIdentifier id) {
}
