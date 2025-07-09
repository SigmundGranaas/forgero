package com.sigmundgranaas.forgero.core.property.api.custom;

import com.sigmundgranaas.forgero.data.loading.api.data.condition.ConditionData;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for raw property data records loaded from JSON.
 * Ensures that the data structure provides a (possibly null) ConditionData object.
 */
public interface CustomPropertyData {
	@Nullable
	ConditionData condition();
}
