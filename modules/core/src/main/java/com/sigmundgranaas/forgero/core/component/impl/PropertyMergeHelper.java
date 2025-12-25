package com.sigmundgranaas.forgero.core.component.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for merging property maps in component implementations.
 * Provides a consistent merge strategy across all component types.
 */
public final class PropertyMergeHelper {

	private PropertyMergeHelper() {
		// Static utility class
	}

	/**
	 * Merges new properties into existing properties using additive semantics.
	 * For keys that exist in both maps, the lists are concatenated.
	 *
	 * @param existing      the existing property map
	 * @param newProperties the new properties to merge in
	 * @return an unmodifiable merged property map
	 */
	public static Map<String, List<?>> merge(Map<String, List<?>> existing, Map<String, List<?>> newProperties) {
		Map<String, List<?>> merged = new HashMap<>(existing);
		newProperties.forEach((key, value) -> merged.merge(key, value, (existingList, incomingList) -> {
			List<Object> combined = new ArrayList<>(existingList);
			combined.addAll(incomingList);
			return combined;
		}));
		return Collections.unmodifiableMap(merged);
	}
}
