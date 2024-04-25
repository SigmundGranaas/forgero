package com.sigmundgranaas.forgero.core.state.identity;

import java.util.function.Function;

import com.sigmundgranaas.forgero.core.state.State;

public interface ModificationRule {
	boolean applies(State state);

	Function<String, String> transformation();
}
