package com.sigmundgranaas.forgero.state;

import com.sigmundgranaas.forgero.core.state.MutableStateProvider;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class MutableStateProviderTest {

	@Test
	void updateStateWithValidUpgradeSucceeds() {
		var defaultState = State.of("teststate", Type.UNDEFINED, Collections.emptyList());
		var updateState = State.of("teststate", Type.UNDEFINED, Collections.emptyList());
		var mutableProvider = new MutableStateProvider(defaultState);

		Assertions.assertEquals(mutableProvider.update(updateState).get(), updateState);
	}

	@Test
	void updateStateWithInvalidUpgradeFails() {
		var defaultState = State.of("teststate", Type.UNDEFINED, Collections.emptyList());
		var updateState = State.of("wrongstate", Type.UNDEFINED, Collections.emptyList());
		var mutableProvider = new MutableStateProvider(defaultState);

		Assertions.assertNotEquals(mutableProvider.update(updateState).get(), updateState);
	}

	@Test
	void canUpdate() {
		var defaultState = State.of("teststate", Type.UNDEFINED, Collections.emptyList());
		var updateState = State.of("teststate", Type.UNDEFINED, Collections.emptyList());
		var mutableProvider = new MutableStateProvider(defaultState);

		Assertions.assertTrue(mutableProvider.canUpdate(updateState));
	}
}
