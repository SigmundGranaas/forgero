package com.sigmundgranaas.forgero.core.state.identity.sorting;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;

public interface SortingRule {
	static SortingRule of(Type type, int priority) {
		return new TypeRule(type, priority);
	}

	boolean applies(State state);

	int getPriority();
}
