package com.sigmundgranaas.forgero.cof.dto;

import java.util.List;

/**
 * DTO for a serialized ComponentUpgrades object.
 *
 * @param slots A list of serialized upgrade slots.
 */
public record CofUpgrades(List<CofUpgradeSlot> slots) {
}
