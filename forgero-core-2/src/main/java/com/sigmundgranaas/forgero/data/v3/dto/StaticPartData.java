package com.sigmundgranaas.forgero.data.v3.dto;

import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier; // Import OpenIdentifier
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * DTO for `forgero:static_part` type data files.
 * Defines a single, non-generated component.
 *
 * @param type       The type identifier, always "forgero:static_part".
 * @param name       The unique name of the static part.
 * @param include    Optional list of IDs of other definitions to include.
 * @param tags       Optional list of tags associated with the static part.
 * @param attributes Optional list of attributes inherent to this static part.
 * @param features   Optional list of features inherent to this static part.
 */
public record StaticPartData(
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
