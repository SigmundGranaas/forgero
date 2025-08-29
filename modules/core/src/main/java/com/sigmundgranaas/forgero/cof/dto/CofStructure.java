package com.sigmundgranaas.forgero.cof.dto;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Map;

/**
 * DTO representing the required structural composition of a component.
 * Uses a map for direct, key-based access to slots.
 *
 * @param slots A map where keys are unique slot names (e.g., "head") and values are the CofSlot DTOs.
 */
public record CofStructure(Map<OpenIdentifier, CofSlot> slots) {}
