// File: /home/sigmund/Documents/projects/forgero/1-20/forgero-core-2/src/main/java/com/sigmundgranaas/forgero/data/v3/dto/GeneratedEquipmentData.java
package com.sigmundgranaas.forgero.data.v3.dto;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.UpgradeSlotData;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * DTO for a generated, concrete equipment item (e.g., an Iron Pickaxe with Oak Handle).
 * This is the result of combining a `ToolTemplateData` with specific `PartData` instances.
 * It contains all resolved properties and concrete references to its constituent parts.
 */
public record GeneratedEquipmentData(
		OpenIdentifier type, // The type from the original tool template (e.g., "forgero:tool_template")
		String name,
		@Nullable
		List<OpenIdentifier> tags,
		EquipmentTemplateStructureData structure, // Contains concrete part IDs
		@Nullable
		List<UpgradeSlotData> upgrades,
		@Nullable
		List<AttributeData> attributes,
		@Nullable
		List<FeatureData> features
){

	// Generated components do not have an `id` field in their raw DTO,
	// their `id` is external (from `IdentifiedTopLevelData`).
	// However, the `TopLevelData` interface requires `id()`. This DTO should represent the data
	// *contained within* the `IdentifiedTopLevelData` wrapper.
	// For simplicity, we'll omit `id()` here and rely on the wrapper for it.
	// Generated components do not have 'include'

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
