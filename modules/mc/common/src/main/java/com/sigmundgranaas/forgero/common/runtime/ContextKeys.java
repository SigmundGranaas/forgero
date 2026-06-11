package com.sigmundgranaas.forgero.common.runtime;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import java.util.Set;

/**
 * A central registry for predefined, type-safe keys used in the DynamicContext.
 * This ensures that both the game-specific module (populating the context) and
 * the core module (reading from the context) use the same, well-defined keys.
 */
public class ContextKeys {
	/**
	 * A key to retrieve the set of tags associated with a target entity.
	 * Type: Set<OpenIdentifier>
	 */
	public static final Key<Set<OpenIdentifier>> TARGET_TAGS = new Key<>(new OpenIdentifier("forgero", "target_tags"));

	// Example of another potential key:
	// public static final Key<Integer> WORLD_TIME = new Key<>(new OpenIdentifier("forgero", "world_time"));
}
