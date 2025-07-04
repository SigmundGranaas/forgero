package com.sigmundgranaas.forgero.data.v3.dto;

import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier; // Import OpenIdentifier
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * DTO for `forgero:material` type data files.
 * Defines a base material like iron or diamond.
 *
 * @param type       The type identifier, always "forgero:material".
 * @param name       The unique name of the material.
 * @param include    Optional list of IDs of other definitions to include.
 * @param tags       Optional list of tags associated with the material.
 * @param attributes Optional list of attributes provided by this material.
 * @param features   Optional list of features provided by this material.
 */
public record MaterialData(
		OpenIdentifier type, // Changed from String
		String name,
		@Nullable
		List<OpenIdentifier> include, // Changed from List<String>
		@Nullable
		List<OpenIdentifier> tags, // Changed from List<String>
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
