package com.sigmundgranaas.forgero.core.property.v2.feature;

import com.sigmundgranaas.forgero.core.property.Property;

public interface Feature extends Property {
	static Feature of(String type) {
		return () -> type;
	}

	static ClassKey<Feature> key(String type) {
		return new ClassKey<>(type, Feature.class);
	}

	String type();
}
