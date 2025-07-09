package com.sigmundgranaas.forgero.property.tooltip;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.condition.Condition;

import javax.annotation.Nullable;

/**
 * Represents a single entry for a custom tooltip section, e.g., "ingredient_count".
 * These entries are designed to be collected and displayed together.
 */
public record TooltipProperty(
		OpenIdentifier key,
		String value,
		String format,
		@Nullable Condition localCondition
) implements ConditionalProperty {

	@Override
	@Nullable
	public Condition condition() {
		return localCondition;
	}
}
