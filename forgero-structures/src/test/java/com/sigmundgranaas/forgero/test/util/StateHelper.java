package com.sigmundgranaas.forgero.test.util;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;

public class StateHelper {
    public static State state(String id){
        return ForgeroStateRegistry.STATES.get(id).orElseThrow();
    }
    public static Composite composite(String id){
        return (Composite) ForgeroStateRegistry.STATES.get(id).orElseThrow();
    }
    public static Composite.CompositeBuilder builder(String id){
        return composite(id).toBuilder();
    }
}
