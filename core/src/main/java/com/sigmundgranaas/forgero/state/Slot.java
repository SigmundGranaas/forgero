package com.sigmundgranaas.forgero.state;

import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.Optional;

public interface Slot extends PropertyContainer, Matchable {
    int index();

    boolean filled();

    Optional<State> get();

    Optional<Slot> fill(State slottable);

}
