package com.sigmundgranaas.forgero.core.property.v2.feature;

import java.util.Optional;

import com.google.gson.JsonElement;

public interface FeatureBuilder<T extends Feature> {
	Optional<T> build(JsonElement element);
}
