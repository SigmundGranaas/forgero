package com.sigmundgranaas.forgero.core.property.v2.cache;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;

import java.util.Objects;

public record FeatureContainerKey(ContainerTargetPair pair, ClassKey<? extends Feature> key) {
	public static FeatureContainerKey of(PropertyContainer container, ClassKey<? extends Feature> key) {
		return new FeatureContainerKey(ContainerTargetPair.of(container), key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pair, key);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FeatureContainerKey that = (FeatureContainerKey) o;
		return Objects.equals(pair, that.pair) && Objects.equals(key, that.key);
	}

	@Override
	public String toString() {
		return String.format("%s-%s", pair.hashCode(), key);
	}
}
