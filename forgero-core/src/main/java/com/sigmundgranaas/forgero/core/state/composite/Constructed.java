package com.sigmundgranaas.forgero.core.state.composite;

import com.sigmundgranaas.forgero.core.state.State;

import java.util.List;

public interface Constructed {
    List<State> parts();
}
