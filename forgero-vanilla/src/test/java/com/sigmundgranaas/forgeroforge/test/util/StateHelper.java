package com.sigmundgranaas.forgeroforge.test.util;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;

public class StateHelper {
    public static State state(String id) {
        return ForgeroStateRegistry.stateFinder().find(id).orElseThrow();
    }

    public static Composite composite(String id) {
        return (Composite) ForgeroStateRegistry.stateFinder().find(id).orElseThrow();
    }

}
