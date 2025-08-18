package com.sigmundgranaas.forgero.data.loading.api.data;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;


/**
 * DTO for `forgero:material` type data files.
 * Defines a base material like iron or diamond.
 *
 * @param type       The type identifier, always "forgero:material".
 * @param name       The unique name of the material.
 * @param include    Optional list of IDs of other definitions to include.
 * @param tags       Optional list of tags associated with the material.
 * @param host       Optional data for mapping to a platform-specific item.
 * @param attributes Optional list of attributes provided by this material.
 * @param properties Optional map for custom, extensible properties.
 */
public record MaterialData(
		OpenIdentifier type,
		String name,
		@Nullable
		List<OpenIdentifier> include,
		@Nullable
		List<OpenIdentifier> tags,
		@Nullable
		HostData host,
		@Nullable
		List<AttributeData> attributes,
		@Nullable
		Map<String, JsonElement> properties
){}
