package com.sigmundgranaas.forgero.data.loading.api.data;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import org.jetbrains.annotations.Nullable;
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
 * @param properties Optional map for custom, extensible properties.
 */
public record SchematicData(
		OpenIdentifier type,
		String name,
		@Nullable
		List<OpenIdentifier> include,
		@Nullable
		List<OpenIdentifier> tags,
		OpenIdentifier target,
		String craftingMaterial,
		@Nullable
		Map<String, JsonElement> properties
) implements PropertyContainer {
	// Schematics typically don't have attributes or features directly, but can inherit properties.
	@Override
	public @Nullable List<AttributeData> attributes() { return null; }
	@Override
	public @Nullable List<FeatureData> features() { return null; }
}
