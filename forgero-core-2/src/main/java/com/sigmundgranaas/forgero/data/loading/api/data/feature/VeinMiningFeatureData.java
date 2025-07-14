package com.sigmundgranaas.forgero.data.loading.api.data.feature;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
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
		OpenIdentifier type,
		String title,
		String description,
		VeinMiningSelectorData selector,
		@Nullable
		Condition condition
) implements FeatureData {
}
