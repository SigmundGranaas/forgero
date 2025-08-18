package com.sigmundgranaas.forgero.cof.dto;

import java.util.List;

/**
 * DTO representing the optional upgrade slots of a component.
 * Uses a list as upgrade slots are ordered and may not have unique names.
 *
 * @param slots The list of CofSlot DTOs representing the upgrade slots.
 */
public record CofUpgrades(List<CofSlot> slots) {}
