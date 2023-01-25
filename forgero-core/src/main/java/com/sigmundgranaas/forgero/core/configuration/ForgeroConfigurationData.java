package com.sigmundgranaas.forgero.core.configuration;

public interface ForgeroConfigurationData {
	default void setByKey(String key, Object value) {
		try {
			getClass().getField(key).set(this, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	default Object getByKey(String key) {
		try {
			return getClass().getField(key).get(this);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
}
