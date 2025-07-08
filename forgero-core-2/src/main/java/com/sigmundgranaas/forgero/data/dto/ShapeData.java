package com.sigmundgranaas.forgero.data.dto;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.dto.feature.FeatureData;
import org.jetbrains.annotations.Nullable;
import java.util.List;


/**
 * DTO for `forgero:shape` type data files.
 * Defines a geometric shape that can be combined with a material to form a part.
 *
 * @param type       The type identifier, always "forgero:shape".
 * @param name       The unique name of the shape.
 * @param include    Optional list of IDs of other definitions to include.
 * @param tags       Optional list of tags associated with the shape.
 * @param attributes Optional list of attributes provided by this shape.
 * @param features   Optional list of features provided by this shape.
 */
public record ShapeData(
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
