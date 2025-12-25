package com.sigmundgranaas.forgero.data.loading.api.data;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * DTO for `forgero:cast` type data files.
 * Defines a cast that extends a shape and provides casting bonuses.
 *
 * <p>Casts extend shapes via the {@code include} field and add bonus attributes
 * that apply when the cast is used in the shape slot of a template.</p>
 *
 * <p>Example use case: A pickaxe_head_cast extends pickaxe_head shape and provides
 * a 1.2x durability multiplier. When filled with iron, it creates the same
 * iron-pickaxe_head item as the base shape or schematic, but with different bonuses.</p>
 *
 * @param type            The type identifier, always "forgero:cast".
 * @param name            The unique name of the cast.
 * @param include         Optional list of IDs of other definitions to include (typically a shape).
 * @param tags            Optional list of tags associated with the cast (inherited via include).
 * @param localTags       Optional list of tags LOCAL to this cast (NOT inherited via include).
 * @param host            Optional data for mapping to a platform-specific item.
 * @param attributes      Optional list of attributes provided by this cast (inherited via include).
 * @param localAttributes Optional list of attributes LOCAL to this cast (NOT inherited via include).
 * @param properties      Optional map for custom, extensible properties.
 */
public record CastData(
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
		Map<String, JsonElement> properties
) implements ResourceTypeData {
}
