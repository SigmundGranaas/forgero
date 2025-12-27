package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shared logic for all component implementations.
 * Used via composition to avoid boilerplate while preserving record immutability.
 */
public final class ComponentCore {
	private ComponentCore() {
	}

	/**
	 * Returns a non-null, unmodifiable set of tags.
	 *
	 * @param tags The tags set, which may be null
	 * @return The original set if non-null, or an empty set
	 */
	public static Set<OpenIdentifier> getTags(Set<OpenIdentifier> tags) {
		return tags != null ? tags : Collections.emptySet();
	}

	/**
	 * Merges new properties into existing properties.
	 *
	 * @param current       The current properties map
	 * @param newProperties The new properties to merge
	 * @return A merged properties map
	 */
	public static Map<String, List<?>> mergeProperties(
			Map<String, List<?>> current,
			Map<String, List<?>> newProperties) {
		return PropertyMergeHelper.merge(current, newProperties);
	}
}
