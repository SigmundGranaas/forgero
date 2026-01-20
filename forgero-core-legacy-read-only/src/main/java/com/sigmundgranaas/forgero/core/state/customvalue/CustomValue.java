package com.sigmundgranaas.forgero.core.state.customvalue;

public interface CustomValue {
	static CustomValue of(String id, String value) {
		return new CustomStringValue(id, value);
	}

	String identifier();

	String presentableValue();
}
