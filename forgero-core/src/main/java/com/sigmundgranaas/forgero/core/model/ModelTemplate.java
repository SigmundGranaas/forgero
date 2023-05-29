package com.sigmundgranaas.forgero.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ModelEntryData;
import com.sigmundgranaas.forgero.core.texture.utils.Offset;

public interface ModelTemplate {
	int order();

	Optional<Offset> getOffset();

	default Integer getResolution() {
		return 16;
	}

	default Optional<JsonObject> getDisplayOverrides() {
		return Optional.empty();
	}

	default List<ModelEntryData> getSecondaryTextures() {
		return Collections.emptyList();
	}

	<T> T convert(Converter<T, ModelTemplate> converter);
}
