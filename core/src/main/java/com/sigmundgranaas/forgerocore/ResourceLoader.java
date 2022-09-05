package com.sigmundgranaas.forgerocore;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgerocore.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgerocore.resource.ForgeroResource;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ResourceLoader<R extends ForgeroResource<T>, T extends ForgeroDataResource> {
    @NotNull ImmutableList<R> loadResources();

    default @NotNull ImmutableList<T> loadPojos() {
        return ImmutableList.<T>builder().build();
    }
}
