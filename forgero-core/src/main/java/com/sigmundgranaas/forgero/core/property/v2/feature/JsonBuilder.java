package com.sigmundgranaas.forgero.core.property.v2.feature;

import java.util.Optional;

import com.google.gson.JsonElement;

public interface JsonBuilder<T> {
	Optional<T> build(JsonElement element);

	Class<T> getTargetClass();
}
