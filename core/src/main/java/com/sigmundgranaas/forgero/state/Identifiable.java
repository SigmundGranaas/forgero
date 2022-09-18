package com.sigmundgranaas.forgero.state;

public interface Identifiable {
    String name();

    String nameSpace();

    default String identifier() {
        return String.format("%s#%s", nameSpace(), name());
    }
}
