package com.sigmundgranaas.forgero.core.property.v2.feature;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.util.TypeToken;

public interface JsonBuilder<T> {
	Optional<T> build(JsonElement element);

	TypeToken<T> getTargetClass();
}
