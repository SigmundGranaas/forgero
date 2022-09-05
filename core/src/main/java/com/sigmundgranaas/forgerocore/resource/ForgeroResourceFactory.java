package com.sigmundgranaas.forgerocore.resource;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgerocore.data.v1.ForgeroDataResource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ForgeroResourceFactory<R extends ForgeroResource<T>, T extends ForgeroDataResource> {
    @NotNull
    Optional<R> createResource(T data);

    Optional<R> buildResource(T pojo);

    @NotNull
    ImmutableList<R> createResources();
}
