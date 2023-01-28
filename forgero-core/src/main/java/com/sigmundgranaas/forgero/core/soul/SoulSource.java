package com.sigmundgranaas.forgero.core.soul;

public record SoulSource(String id) {

    public String name() {
        if (id == null) {
            return "zombie";
        }
        var elements = id.split(":");
        if (elements.length > 1) {
            return elements[1];
        }

        return id;
    }
}
