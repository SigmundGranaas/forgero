package com.sigmundgranaas.forgero.data.v3.dto.feature;

import com.sigmundgranaas.forgero.data.v3.dto.condition.ConditionData;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier; // Import OpenIdentifier
import org.jetbrains.annotations.Nullable;

/**
 * DTO for the "forgero:vein_mining" feature.
 *
 * @param type        The type identifier for the feature, always "forgero:vein_mining".
 * @param title       The translatable title key for the feature.
 * @param description The translatable description key for the feature.
 * @param selector    The data defining how the vein mining selects blocks.
 * @param condition   An optional condition for when this feature is active.
 */
public record VeinMiningFeatureData(
		OpenIdentifier type, // Changed from String
		String title,
		String description,
		VeinMiningSelectorData selector,
		@Nullable
		ConditionData condition
) implements FeatureData {
}
