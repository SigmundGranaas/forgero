package com.sigmundgranaas.forgero.data.v3.dto;

import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * DTO for `forgero:tool_template` type data files.
 * Defines the structure of a complete tool.
 *
 * @param type       The type identifier, always "forgero:tool_template".
 * @param name       The unique name of the tool template.
 * @param include    Optional list of IDs of other definitions to include.
 * @param tags       Optional list of tags associated with the tool template.
 * @param structure  Defines the required parts for tool assembly.
 * @param upgrades   Optional list of tool-level upgrade slots.
 * @param attributes Optional list of attributes inherent to this tool template.
 * @param features   Optional list of features inherent to this tool template.
 */
public record ToolTemplateData(
		OpenIdentifier type, // Changed from String
		String name,
		@Nullable
		List<OpenIdentifier> include, // Changed from List<String>
		@Nullable
		List<OpenIdentifier> tags, // Changed from List<String>
		ToolTemplateStructureData structure,
		@Nullable
		List<UpgradeSlotData> upgrades,
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
