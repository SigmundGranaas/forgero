package com.sigmundgranaas.forgero.state.slot;

import com.sigmundgranaas.forgero.state.Slot;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.MatchContext;
import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.Optional;

public class FilledSlot extends AbstractTypedSlot {
    private final State upgrade;

    public FilledSlot(int index, Type type, State upgrade) {
        super(index, type);
        this.upgrade = upgrade;
    }

    @Override
    public boolean filled() {
        return true;
    }

    @Override
    public Optional<State> get() {
        return Optional.of(upgrade);
    }

    @Override
    public Optional<Slot> fill(State slottable) {
        return Optional.empty();
    }

    @Override
    public boolean test(Matchable match, MatchContext context) {
        if (test(match)) {
            return true;
        } else {
            return upgrade.test(match);
        }
    }
}
