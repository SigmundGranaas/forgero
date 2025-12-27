package com.sigmundgranaas.forgero.data.loading.api.data.template;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.api.data.GenerationConfigData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.HostTemplateData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;


/**
 * DTO for `forgero:part_template` type data files.
 * Defines the "shape" of a composite part (e.g., a head, a blade).
 *
 * @param type          The type identifier, always "forgero:part_template".
 * @param name          The unique name of the part template.
 * @param include       Optional list of IDs of other definitions to include.
 * @param tags          Optional list of tags associated with the part template.
 * @param host_template Optional template for mapping generated parts to a host platform item.
 * @param structure     Defines the required materials for its construction.
 * @param upgrades      Optional list of upgrade slots available on this part.
 * @param attributes    Optional list of attributes inherent to this part template.
 * @param generation    Optional configuration for controlling which components trigger generation.
 * @param properties    Optional map for custom, extensible properties.
 */
public record PartTemplateData(
		OpenIdentifier type,
		String name,
		@Nullable
		List<OpenIdentifier> include,
		@Nullable
		List<OpenIdentifier> tags,
		@Nullable
		HostTemplateData host_template,
		PartTemplateStructureData structure,
		@Nullable
		List<UpgradeSlotData> upgrades,
		@Nullable
		List<AttributeData> attributes,
		@Nullable
		GenerationConfigData generation,
		@Nullable
		Map<String, JsonElement> properties
) implements DefinitionData, TemplateData {
}
