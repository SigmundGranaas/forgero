package com.sigmundgranaas.forgero.data.loading.api.data.template;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.HostTemplateData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;


/**
 * DTO for `forgero:equipment_template` type data files.
 * Defines the structure of a complete tool.
 *
 * @param type          The type identifier, always "forgero:equipment_template".
 * @param name          The unique name of the equipment template.
 * @param include       Optional list of IDs of other definitions to include.
 * @param tags          Optional list of tags associated with the equipment template.
 * @param host_template Optional template for mapping generated items to a host platform item.
 * @param structure     Defines the required parts for equipment assembly.
 * @param upgrades      Optional list of equipment-group upgrade slots.
 * @param attributes    Optional list of attributes inherent to this equipment template.
 * @param properties    Optional map for custom, extensible properties.
 */
public record EquipmentTemplateData(
		OpenIdentifier type,
		String name,
		@Nullable
		List<OpenIdentifier> include,
		@Nullable
		List<OpenIdentifier> tags,
		@Nullable
		HostTemplateData host_template,
		EquipmentTemplateStructureData structure,
		@Nullable
		List<UpgradeSlotData> upgrades,
		@Nullable
		List<AttributeData> attributes,
		@Nullable
		Map<String, JsonElement> properties
) implements TemplateData {
}
