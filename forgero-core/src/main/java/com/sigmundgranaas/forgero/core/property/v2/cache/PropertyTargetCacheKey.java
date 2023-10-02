package com.sigmundgranaas.forgero.core.property.v2.cache;

import java.util.Objects;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;

public record PropertyTargetCacheKey(ContainerTargetPair pair, String key) {
	public static PropertyTargetCacheKey of(PropertyContainer container, String key) {
		return new PropertyTargetCacheKey(ContainerTargetPair.of(container), key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pair, key);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PropertyTargetCacheKey that = (PropertyTargetCacheKey) o;
		return Objects.equals(pair, that.pair) && Objects.equals(key, that.key);
	}

	@Override
	public String toString() {
		return String.format("%s-%s", pair.hashCode(), key);
	}
}
