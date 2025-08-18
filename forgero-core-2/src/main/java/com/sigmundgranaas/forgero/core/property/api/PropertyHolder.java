package com.sigmundgranaas.forgero.core.property.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PropertyHolder {
	default <T> List<T> properties(PropertyKey<T> key){
		try {
			return Optional.ofNullable(propertiesAsMap().get(key.key())).map(properties -> (List<T>) properties).orElse(Collections.emptyList());
		}catch (RuntimeException e) {
			return Collections.emptyList();
		}
	}

	default List<?> properties(String key) {
		return Optional.ofNullable(propertiesAsMap().get(key)).orElse(Collections.emptyList());
	}

	Map<String, List<?>> propertiesAsMap();
}
