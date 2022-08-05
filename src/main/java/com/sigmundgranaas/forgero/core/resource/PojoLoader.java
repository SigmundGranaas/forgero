package com.sigmundgranaas.forgero.core.resource;

import com.sigmundgranaas.forgero.core.data.v1.ForgeroDataResource;

import java.util.Collection;

public interface PojoLoader<T extends ForgeroDataResource> {
    Collection<T> loadPojos();
}
