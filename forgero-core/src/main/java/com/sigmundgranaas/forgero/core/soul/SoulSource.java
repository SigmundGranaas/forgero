package com.sigmundgranaas.forgero.core.soul;

public record SoulSource(String id) {

    public String name() {
        return id.split(":")[1];
    }
}
