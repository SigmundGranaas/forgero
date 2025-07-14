package com.sigmundgranaas.forgero.data.loading.api.data.attribute;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import org.jetbrains.annotations.Nullable;

public record AttributeDataImpl(
		OpenIdentifier id,
		OpenIdentifier type,
		ComputationData computation,
		@Nullable Condition condition,
		@Nullable OpenIdentifier composite
) implements AttributeData {
}
