package com.sigmundgranaas.forgero.data.loading.api.data.feature;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import org.jetbrains.annotations.Nullable;

/**
 * An interface representing a single feature definition.
 * This can be implemented by any class to create a new, custom feature type.
 */
public interface FeatureData extends PropertyData {
	/**
	 * @return The unique type identifier for this feature, e.g., "forgero:vein_mining". This string is used
	 * to look up the correct Codec in the registry.
	 */
	OpenIdentifier type();

	/**
	 * @return An optional condition that must be met for this feature to be active.
	 */
	@Nullable
	Condition condition();
}
