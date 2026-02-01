package com.sigmundgranaas.forgero.data.loading.api.data.attribute;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;

import java.util.Optional;

public record AttributeDataImpl(
		Optional<OpenIdentifier> id,
		OpenIdentifier type,
		ComputationData computation,
		Optional<OpenIdentifier> scope,
		Optional<Condition> condition
) implements AttributeData {

	/**
	 * Creates an AttributeDataImpl with no scope (default behavior).
	 */
	public static AttributeDataImpl withoutScope(
			Optional<OpenIdentifier> id,
			OpenIdentifier type,
			ComputationData computation,
			Optional<Condition> condition
	) {
		return new AttributeDataImpl(id, type, computation, Optional.empty(), condition);
	}
}
