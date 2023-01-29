package com.sigmundgranaas.forgero.core.soul;

import com.sigmundgranaas.forgero.core.state.Identifiable;

public record SoulSource(String id, String name) implements Identifiable {


    public String name() {
        if (name == null) {
            return "zombie";
        }
        return name;
    }

    @Override
    public String nameSpace() {
        var elements = id.split(":");
        if (elements.length > 1) {
            return elements[0];
        }
        return id;
    }

    @Override
    public String identifier() {
        return id;
    }
}
