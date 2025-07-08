package com.sigmundgranaas.forgero.data.v3.dto.template;

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
 * DTO for `forgero:part_template` type data files.
 * Defines the "shape" of a composite part (e.g., a head, a blade).
 *
 * @param type       The type identifier, always "forgero:part_template".
 * @param name       The unique name of the part template.
 * @param include    Optional list of IDs of other definitions to include.
 * @param tags       Optional list of tags associated with the part template.
 * @param structure  Defines the required materials for its construction.
 * @param upgrades   Optional list of upgrade slots available on this part.
 * @param naming     Optional naming pattern for generated parts.
 * @param attributes Optional list of attributes inherent to this part template.
 * @param features   Optional list of features inherent to this part template.
 */
public record PartTemplateData(
		OpenIdentifier type, // Changed from String
		String name,
		@Nullable
		List<OpenIdentifier> include, // Changed from List<String>
		@Nullable
		List<OpenIdentifier> tags, // Changed from List<String>
		PartTemplateStructureData structure,
		@Nullable
		List<UpgradeSlotData> upgrades,
		@Nullable
		PartTemplateNamingData naming,
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
