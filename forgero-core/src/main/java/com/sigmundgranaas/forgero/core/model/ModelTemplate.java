package com.sigmundgranaas.forgero.core.model;

import java.util.Optional;

import com.google.gson.JsonObject;
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

	<T> T convert(Converter<T, ModelTemplate> converter);
}
