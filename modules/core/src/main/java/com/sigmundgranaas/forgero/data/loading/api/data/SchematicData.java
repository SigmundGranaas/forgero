package com.sigmundgranaas.forgero.data.loading.api.data;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;


/**
 * DTO for `forgero:schematic` type data files.
 * Defines a schematic that extends a shape and provides crafting bonuses.
 *
 * <p>Schematics extend shapes via the {@code include} field and add bonus attributes
 * that apply when the schematic is used in the shape slot of a template.</p>
 *
 * @param type            The type identifier, always "forgero:schematic".
 * @param name            The unique name of the schematic.
 * @param include         Optional list of IDs of other definitions to include (typically a shape).
 * @param tags            Optional list of tags associated with the schematic (inherited via include).
 * @param localTags       Optional list of tags LOCAL to this schematic (NOT inherited via include).
 * @param host            Optional data for mapping to a platform-specific item.
 * @param attributes      Optional list of attributes provided by this schematic (inherited via include).
 * @param localAttributes Optional list of attributes LOCAL to this schematic (NOT inherited via include).
 * @param target          (DEPRECATED) The ID of the `part_template` this schematic crafts. Kept for backward compatibility.
 * @param properties      Optional map for custom, extensible properties.
 */
public record SchematicData(
		OpenIdentifier type,
		String name,
		@Nullable
		List<OpenIdentifier> include,
		@Nullable
		List<OpenIdentifier> tags,
		@Nullable
		List<OpenIdentifier> localTags,
		@Nullable
		HostData host,
		@Nullable
		List<AttributeData> attributes,
		@Nullable
		List<AttributeData> localAttributes,
		@Nullable
		OpenIdentifier target,
		@Nullable
		Map<String, JsonElement> properties
) implements ResourceTypeData {
}
