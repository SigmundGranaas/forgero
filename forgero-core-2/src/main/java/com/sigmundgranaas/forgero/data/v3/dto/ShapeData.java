// src/main/java/com/sigmundgranaas/forgero/data/v3/dto/ShapeData.java
package com.sigmundgranaas.forgero.data.v3.dto;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * DTO for `forgero:shape` type data files.
 * Defines a geometric shape that can be combined with a material to form a part.
 *
 * @param type       The type identifier, always "forgero:shape".
 * @param name       The unique name of the shape.
 * @param include    Optional list of IDs of other definitions to include.
 * @param tags       Optional list of tags associated with the shape.
 * @param attributes Optional list of attributes provided by this shape.
 * @param features   Optional list of features provided by this shape.
 */
public record ShapeData(
		OpenIdentifier type,
		String name,
		@Nullable
		List<OpenIdentifier> include,
		@Nullable
		List<OpenIdentifier> tags,
		@Nullable
		List<AttributeData> attributes,
		@Nullable
		List<FeatureData> features
) {
	public Map<OpenIdentifier, AttributeData> getAttributesMap() {
		if (attributes == null) return Collections.emptyMap();
		return attributes.stream().collect(Collectors.toMap(AttributeData::id, Function.identity(), (a1, a2) -> a2));
	}

	public Map<OpenIdentifier, FeatureData> getFeaturesMap() {
		if (features == null) return Collections.emptyMap();
		return features.stream().collect(Collectors.toMap(FeatureData::type, Function.identity(), (f1, f2) -> f2));
	}
}
