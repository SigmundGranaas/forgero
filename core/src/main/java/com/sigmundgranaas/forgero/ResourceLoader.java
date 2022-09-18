package com.sigmundgranaas.forgero;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.resource.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgero.resource.ForgeroResource;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ResourceLoader<R extends ForgeroResource<T>, T extends ForgeroDataResource> {
    @NotNull ImmutableList<R> loadResources();

    default @NotNull ImmutableList<T> loadPojos() {
        return ImmutableList.<T>builder().build();
    }
}
