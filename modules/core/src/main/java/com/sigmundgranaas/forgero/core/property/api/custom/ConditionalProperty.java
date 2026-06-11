package com.sigmundgranaas.forgero.core.property.api.custom;

import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * An interface for any property that can be conditionally applied.
 * It provides a standard way to access its condition. Dynamic conditions are carried
 * as data and evaluated by the game layer (see RuntimeConditions in the mc common module).
 */
public interface ConditionalProperty extends Property {
	@Nullable
	Condition condition();

	/**
	 * Default implementation to wrap the nullable condition in an Optional.
	 */
	default Optional<Condition> getCondition() {
		return Optional.ofNullable(condition());
	}
}
