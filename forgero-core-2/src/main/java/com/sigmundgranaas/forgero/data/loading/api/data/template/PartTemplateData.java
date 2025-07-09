package com.sigmundgranaas.forgero.data.loading.api.data.template;

import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;


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
 * @param attributes Optional list of attributes inherent to this part template.
 * @param features   Optional list of features inherent to this part template.
 */
public record PartTemplateData(
		OpenIdentifier type,
		String name,
		@Nullable
		List<OpenIdentifier> include,
		@Nullable
		List<OpenIdentifier> tags,
		PartTemplateStructureData structure,
		@Nullable
		List<UpgradeSlotData> upgrades,
		@Nullable
		List<AttributeData> attributes,
		@Nullable
		List<FeatureData> features
) {
}
