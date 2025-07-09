package com.sigmundgranaas.forgero.property.namereplacement;

import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.condition.Condition;

import javax.annotation.Nullable;

/**
 * A property that defines a name replacement. This is typically used for items that
 * change their display name based on certain conditions (e.g., if a specific part is present).
 */
public record NameReplacementProperty(
		String from,
		String to,
		@Nullable Condition localCondition
) implements ConditionalProperty {

	@Override
	@Nullable
	public Condition condition() {
		return localCondition;
	}

	public NameReplacementProperty(String from, String to) {
		this(from, to, Condition.ALWAYS_TRUE);
	}
}
