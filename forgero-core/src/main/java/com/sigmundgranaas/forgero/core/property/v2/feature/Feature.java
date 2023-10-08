package com.sigmundgranaas.forgero.core.property.v2.feature;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public interface Feature {
	String type();

	default String id() {
		return EMPTY_IDENTIFIER;
	}
}
