package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.Forgero;
import org.jetbrains.annotations.NotNull;

/**
 * State provider for swapping states
 * Used for reloading state, and providing updated state configurations
 */
public class MutableStateProvider implements StateProvider {
    @NotNull
    private State state;

    public MutableStateProvider(@NotNull State defaultState) {
        state = defaultState;
    }

    @Override
    public State get() {
        return state;
    }

    public StateProvider update(State update) {
        if (canUpdate(update)) {
            this.state = update;
        } else {
            Forgero.LOGGER.warn("Tried to update state: {}, with a new incompatible state: {}", state.identifier(), update.identifier());
        }
        return this;
    }

    public boolean canUpdate(State update) {
        return state.identifier().equals(update.identifier()) && state.type().typeName().equals(update.type().typeName());
    }
}
