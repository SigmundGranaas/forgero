package com.sigmundgranaas.forgero.core.tags.core.engine;

import java.util.Map;

/**
 * An abstraction for a source of tag data.
 * The key is the fully qualified name of the tag (e.g., "forgero:sword_blade").
 * The value is the raw JSON content defining that tag.
 */
@FunctionalInterface
public interface TagSource {
	Map<String, String> getTags();
}
