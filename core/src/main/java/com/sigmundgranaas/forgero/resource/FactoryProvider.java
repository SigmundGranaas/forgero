package com.sigmundgranaas.forgero.resource;

import com.sigmundgranaas.forgero.resource.data.v1.ForgeroDataResource;

import java.util.Collection;

@FunctionalInterface
public interface FactoryProvider<T extends ForgeroResource<R>, R extends ForgeroDataResource> {
    ForgeroResourceFactory<T, R> createFactory(Collection<R> pojos);
}
