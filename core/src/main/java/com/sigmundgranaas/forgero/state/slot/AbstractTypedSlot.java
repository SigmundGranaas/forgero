package com.sigmundgranaas.forgero.state.slot;

import com.sigmundgranaas.forgero.state.Slot;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

public abstract class AbstractTypedSlot implements Slot {
    private final Type type;
    private final int index;
    private final String description;

    public AbstractTypedSlot(int index, Type type, String description) {
        this.index = index;
        this.type = type;
        this.description = description;
    }

    @Override
    public int index() {
        return index;
    }

    public Type type() {
        return type;
    }

    @Override
    public String description() {
        return description.equals(EMPTY_IDENTIFIER) ? type().typeName().toLowerCase() : description;
    }

    @Override
    public String identifier() {
        return description();
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
