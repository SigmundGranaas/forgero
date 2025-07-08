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
 * DTO for a generated, concrete part (e.g., an Iron Pickaxe Head).
 * This is the result of combining a `MaterialData` with a `PartTemplateData`.
 * It contains all resolved properties and concrete references to its constituent material.
 */
public record GeneratedPartData(
		OpenIdentifier type, // The type from the original part template (e.g., "forgero:part_template")
		String name,
		@Nullable
		List<OpenIdentifier> tags,
		PartTemplateStructureData structure, // Contains concrete material ID
		@Nullable
		List<UpgradeSlotData> upgrades,
		@Nullable
		List<AttributeData> attributes,
		@Nullable
		List<FeatureData> features
){


	// Generated components do not have 'include' or 'naming' (as naming is handled during generation)
	public @Nullable List<OpenIdentifier> include() {
		return null;
	}


	public Map<OpenIdentifier, AttributeData> getAttributesMap() {
		if (attributes == null) return Collections.emptyMap();
		return attributes.stream().collect(Collectors.toMap(AttributeData::id, Function.identity(), (a1, a2) -> a2));
	}

	public Map<OpenIdentifier, FeatureData> getFeaturesMap() {
		if (features == null) return Collections.emptyMap();
		return features.stream().collect(Collectors.toMap(FeatureData::type, Function.identity(), (f1, f2) -> f2));
	}
}
