package com.sigmundgranaas.forgero.data.loading.api.data.attribute;

import com.sigmundgranaas.forgero.core.condition.api.Condition;

import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link AttributeBatchData}.
 */
public record AttributeBatchDataImpl(
		Optional<String> idPrefix,
		Optional<Condition> condition,
		Optional<ComputationData> computation,
		Map<String, AttributeValueData> values
) implements AttributeBatchData {
}
