package com.sigmundgranaas.forgero.core.resource;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;

import java.util.Optional;

public interface ForgeroResourceFactory<R extends ForgeroResource<T>, T extends ForgeroDataResource> {
    Optional<R> createResource(T data);

    ImmutableList<R> createResources();
}
