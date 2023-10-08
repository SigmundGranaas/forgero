package com.sigmundgranaas.forgero.core.property.v2.feature;

public record FeatureKey<T>(String type, Class<T> clazz) {
}
