package com.sigmundgranaas.forgero.data.dto.attribute;

import com.sigmundgranaas.forgero.data.dto.condition.ConditionData;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

/**
 * The concrete record implementation for {@link AttributeData}.
 * This record holds the actual data parsed from JSON files.
 */
public record AttributeDataImpl(
		OpenIdentifier id,
		OpenIdentifier type,
		ComputationData computation,
		@Nullable ConditionData condition,
		@Nullable OpenIdentifier composite
) implements AttributeData {
}
