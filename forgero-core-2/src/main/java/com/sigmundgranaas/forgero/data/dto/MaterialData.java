package com.sigmundgranaas.forgero.data.dto;

import com.sigmundgranaas.forgero.data.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;


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
		OpenIdentifier type,
		String name,
		@Nullable
		List<OpenIdentifier> include,
		@Nullable
		List<OpenIdentifier> tags,
		@Nullable
		List<AttributeData> attributes,
		@Nullable
		List<FeatureData> features
) {

}
