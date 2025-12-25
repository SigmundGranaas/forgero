package com.sigmundgranaas.forgero.core.property.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface PropertyHolder {
	/**
	 * Returns properties for the given key, filtering to only those matching the key's type.
	 * Elements that don't match the expected type are silently skipped.
	 */
	default <T> List<T> properties(PropertyKey<T> key) {
		List<?> rawList = propertiesAsMap().get(key.key());
		if (rawList == null || rawList.isEmpty()) {
			return Collections.emptyList();
		}

		List<T> result = new ArrayList<>(rawList.size());
		for (Object item : rawList) {
			if (key.type().isInstance(item)) {
				result.add(key.type().cast(item));
			}
		}
		return Collections.unmodifiableList(result);
	}

	default List<?> properties(String key) {
		List<?> list = propertiesAsMap().get(key);
		return list != null ? list : Collections.emptyList();
	}

	Map<String, List<?>> propertiesAsMap();
}
