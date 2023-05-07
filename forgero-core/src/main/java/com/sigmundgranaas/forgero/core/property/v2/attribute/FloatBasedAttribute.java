package com.sigmundgranaas.forgero.core.property.v2.attribute;

import com.sigmundgranaas.forgero.core.property.v2.Attribute;

public record FloatBasedAttribute(Float value, String key) implements Attribute {
	@Override
	public String key() {
		return key;
	}

	@Override
	public Float asFloat() {
		return value;
	}
}
