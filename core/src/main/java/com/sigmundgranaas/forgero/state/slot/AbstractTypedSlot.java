package com.sigmundgranaas.forgero.state.slot;

import com.sigmundgranaas.forgero.state.Slot;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;

public abstract class AbstractTypedSlot implements Slot {
    private final Type type;
    private final int index;

    public AbstractTypedSlot(int index, Type type) {
        this.index = index;
        this.type = type;
    }

    @Override
    public int index() {
        return index;
    }

    public Type type() {
        return type;
    }

    @Override
    public boolean test(Matchable match, Context context) {
        if (match instanceof State state) {
            return state.type().test(this.type, context);
        } else if (match instanceof Type type) {
            return this.type.test(type, context);
        }
        return false;
    }

}
