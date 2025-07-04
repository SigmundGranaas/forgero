package com.sigmundgranaas.forgero.data.v3.dto.feature;

import com.sigmundgranaas.forgero.data.v3.dto.condition.ConditionData;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier; // Import OpenIdentifier
import org.jetbrains.annotations.Nullable;

/**
 * An interface representing a single feature definition.
 * This can be implemented by any class to create a new, custom feature type.
 */
public interface FeatureData {
	/**
	 * @return The unique type identifier for this feature, e.g., "forgero:vein_mining". This string is used
	 * to look up the correct Codec in the registry.
	 */
	OpenIdentifier type(); // Changed from String

	/**
	 * @return An optional condition that must be met for this feature to be active.
	 */
	@Nullable
	ConditionData condition();
}
