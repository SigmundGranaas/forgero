package com.sigmundgranaas.forgero.core.api.identity.sorting;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;

/**
 * Convenience class to make it faster to create Sorting rules that need a type and an integer.
 */
public class TypeRule implements SortingRule {
	private final Type type;

	private final int priority;

	public TypeRule(Type type, int priority) {
		this.type = type;
		this.priority = priority;
	}

	@Override
	public boolean applies(State state) {
		return state.test(type);
	}

	@Override
	public int getPriority() {
		return priority;
	}
}
