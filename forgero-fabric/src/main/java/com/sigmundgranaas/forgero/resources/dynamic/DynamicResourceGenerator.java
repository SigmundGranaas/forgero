package com.sigmundgranaas.forgero.resources.dynamic;

import net.devtech.arrp.api.RuntimeResourcePack;

@FunctionalInterface
public interface DynamicResourceGenerator {
    void generate(RuntimeResourcePack pack);

    default boolean enabled() {
        return true;
    }
}
