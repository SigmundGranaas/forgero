package com.sigmundgranaas.forgero.core.property.v2.feature;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public class DefaultPredicateFeature extends BasePredicateFeature {
	public DefaultPredicateFeature(BasePredicateData data) {
		super(data);
	}

	@Override
	public String title() {
		if (super.title.equals(EMPTY_IDENTIFIER)) {
			var parts = type.split(":");
			return String.format("feature.%s.%s.title", parts[0], parts[1]);
		}
		return super.title();
	}
}
