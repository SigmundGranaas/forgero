package com.sigmundgranaas.forgero.data.generation.api;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stage 3 Output: A container for definitions of generated, composite items.
 * These DTOs contain a single, consolidated 'properties' map.
 */
public record GeneratedState(
		Map<OpenIdentifier, GeneratedPart> parts,
		Map<OpenIdentifier, GeneratedEquipment> equipment
) {
	public record GeneratedPart(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			OpenIdentifier materialId,
			OpenIdentifier shapeId,
			@Nullable List<UpgradeSlotData> upgrades,
			@Nullable Map<String, JsonElement> properties
	) {
	}

	public record GeneratedEquipment(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			Map<String, OpenIdentifier> structure,
			@Nullable List<UpgradeSlotData> upgrades,
			@Nullable Map<String, JsonElement> properties
	) {
	}
}
