package com.sigmundgranaas.forgero.data.loading.api.data;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
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
 * @param host            Optional data for mapping to a platform-specific item.
 * @param target          The ID of the `part_template` this schematic crafts.
 * @param properties Optional map for custom, extensible properties.
 */
public record SchematicData(
		OpenIdentifier type,
		String name,
		@Nullable
		List<OpenIdentifier> include,
		@Nullable
		List<OpenIdentifier> tags,
		@Nullable
		HostData host,
		OpenIdentifier target,
		@Nullable
		Map<String, JsonElement> properties
) {
}
