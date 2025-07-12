package com.sigmundgranaas.forgero.data.loading.api.data;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;


/**
 * DTO for `forgero:shape` type data files.
 * Defines a geometric shape that can be combined with a material to form a part.
 *
 * @param type       The type identifier, always "forgero:shape".
 * @param name       The unique name of the shape.
 * @param include    Optional list of IDs of other definitions to include.
 * @param tags       Optional list of tags associated with the shape.
 * @param host       Optional data for mapping to a platform-specific item.
 * @param attributes Optional list of attributes provided by this shape.
 * @param features   Optional list of features provided by this shape.
 * @param properties Optional map for custom, extensible properties.
 */
public record ShapeData(
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
		List<FeatureData> features,
		@Nullable
		Map<String, JsonElement> properties
) implements PropertyContainer {
}
