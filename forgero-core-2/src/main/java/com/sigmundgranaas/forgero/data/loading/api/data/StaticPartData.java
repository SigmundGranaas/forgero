package com.sigmundgranaas.forgero.data.loading.api.data;

import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import org.jetbrains.annotations.Nullable;
import java.util.List;


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
		List<UpgradeSlotData> upgrades,
		@Nullable
		List<FeatureData> features
) {
}
