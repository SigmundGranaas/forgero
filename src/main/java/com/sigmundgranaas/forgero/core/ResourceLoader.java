package com.sigmundgranaas.forgero.core;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;
import org.jetbrains.annotations.NotNull;

public interface ResourceLoader<R extends ForgeroResource<T>, T extends ForgeroDataResource> {
    @NotNull ImmutableList<R> loadResources();

    @NotNull ImmutableList<T> loadPojos();
}
