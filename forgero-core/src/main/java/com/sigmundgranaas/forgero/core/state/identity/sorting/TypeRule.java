package com.sigmundgranaas.forgero.core.state.identity.sorting;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;

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
