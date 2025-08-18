package com.sigmundgranaas.forgero.core.property.api;

public record PropertyKey<T>(Class<T> type, String key) {
}
