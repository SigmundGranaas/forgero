package com.sigmundgranaas.forgero.data.dto.template;

import com.sigmundgranaas.forgero.data.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;


/**
 * DTO for `forgero:tool_template` type data files.
 * Defines the structure of a complete tool.
 *
 * @param type       The type identifier, always "forgero:tool_template".
 * @param name       The unique name of the tool template.
 * @param include    Optional list of IDs of other definitions to include.
 * @param tags       Optional list of tags associated with the tool template.
 * @param structure  Defines the required parts for tool assembly.
 * @param upgrades   Optional list of tool-group upgrade slots.
 * @param attributes Optional list of attributes inherent to this tool template.
 * @param features   Optional list of features inherent to this tool template.
 */
public record EquipmentTemplateData(
		OpenIdentifier type,
		String name,
		@Nullable
		List<OpenIdentifier> include,
		@Nullable
		List<OpenIdentifier> tags,

		EquipmentTemplateStructureData structure,
		@Nullable
		List<UpgradeSlotData> upgrades,
		@Nullable
		List<AttributeData> attributes,
		@Nullable
		List<FeatureData> features
) {
}
