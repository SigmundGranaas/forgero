package com.sigmundgranaas.forgero.core;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ResourceLoader<R extends ForgeroResource<T>, T extends ForgeroDataResource> {
    @NotNull ImmutableList<R> loadResources();

    default @NotNull ImmutableList<T> loadPojos() {
        return ImmutableList.<T>builder().build();
    }
}
