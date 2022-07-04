package com.sigmundgranaas.forgero.core.resource;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;

import java.util.Collection;

@FunctionalInterface
public interface FactoryProvider<T extends ForgeroResource, R extends ForgeroDataResource> {
    ForgeroResourceFactory<T, R> createFactory(Collection<R> pojos);
}
