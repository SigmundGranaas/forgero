package com.sigmundgranaas.forgero.data.loading.api.data;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;
import java.util.List;


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
		OpenIdentifier type,
		String name,
		@Nullable
		List<OpenIdentifier> include,
		@Nullable
		List<OpenIdentifier> tags,
		OpenIdentifier target,
		String craftingMaterial
) {
}
