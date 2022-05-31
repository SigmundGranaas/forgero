package com.sigmundgranaas.forgero.core;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;

public interface ResourceLoader<R extends ForgeroResource<T>, T extends ForgeroDataResource> {
    ImmutableList<R> loadResources();
}
