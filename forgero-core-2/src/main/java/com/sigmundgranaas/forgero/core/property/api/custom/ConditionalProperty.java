package com.sigmundgranaas.forgero.core.property.api.custom;

import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * An interface for any property that can be conditionally applied.
 * It provides a standard way to access its condition and test its dynamic validity.
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

	/**
	 * Helper method to test if the property is active based on its dynamic conditions.
	 *
	 * @param context The dynamic context for the check.
	 * @return true if the property has no dynamic conditions or if all dynamic conditions pass.
	 */
	default boolean test(DynamicContext context) {
		return getCondition()
				.map(c -> c.dynamicConditions().stream().allMatch(cond -> cond.test(context)))
				.orElse(true);
	}
}
