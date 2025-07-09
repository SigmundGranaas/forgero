package com.sigmundgranaas.forgero.data.loading.api.data;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * An interface for any DTO that can hold Forgero properties.
 * This provides a common way for mappers and processors to access both dedicated
 * property fields (like attributes and features) and the generic properties map.
 */
public interface PropertyContainer {
	@Nullable
	List<AttributeData> attributes();

	@Nullable
	List<FeatureData> features();

	@Nullable
	Map<String, JsonElement> properties();
}
