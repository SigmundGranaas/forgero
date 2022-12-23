package com.sigmundgranaas.forgero.state;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.type.Type;

public class EmptyState implements State {

    public static State EMPTY_STATE = new EmptyState();

    @Override
    public String name() {
        return "empty";
    }

    @Override
    public String nameSpace() {
        return Forgero.NAMESPACE;
    }

    @Override
    public Type type() {
        return Type.UNDEFINED;
    }
}
