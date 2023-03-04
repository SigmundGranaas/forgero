package com.sigmundgranaas.forgero.core.model;

import com.sigmundgranaas.forgero.core.texture.utils.Offset;

import java.util.Optional;

public interface ModelTemplate {
	int order();

	Optional<Offset> getOffset();

	<T> T convert(Converter<T, ModelTemplate> converter);
}
