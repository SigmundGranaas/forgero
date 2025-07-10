package com.sigmundgranaas.forgero.cof.dto;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Map;

/**
 * DTO for a serialized ComponentStructure object.
 *
 * @param slots A map from slot ID to the recursively serialized component in that slot.
 */
public record CofStructure(Map<OpenIdentifier, CofComponent> slots) {
}
