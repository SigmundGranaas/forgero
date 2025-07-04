package com.sigmundgranaas.forgero.data.v3.dto;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier; // Import OpenIdentifier
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData; // Added for map getters
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData; // Added for map getters
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * DTO for `forgero:schematic` type data files.
 * Defines a recipe or pattern used to craft a `part_template`.
 *
 * @param type            The type identifier, always "forgero:schematic".
 * @param name            The unique name of the schematic.
 * @param include         Optional list of IDs of other definitions to include.
 * @param tags            Optional list of tags associated with the schematic.
 * @param target          The ID of the `part_template` this schematic crafts.
 * @param craftingMaterial The item ID consumed to make the craft (e.g., "minecraft:paper").
 */
public record SchematicData(
		OpenIdentifier type, // Changed from String
		String name,
		@Nullable
		List<OpenIdentifier> include, // Changed from List<String>
		@Nullable
		List<OpenIdentifier> tags, // Changed from List<String>
		OpenIdentifier target, // Changed from String
		String craftingMaterial // Remains String (Minecraft ID)
) {
	// Schematics typically don't contribute attributes/features for item resolution, so return empty maps.
	public Map<OpenIdentifier, AttributeData> getAttributesMap() {
		return Collections.emptyMap();
	}

	public Map<OpenIdentifier, FeatureData> getFeaturesMap() {
		return Collections.emptyMap();
	}
}
