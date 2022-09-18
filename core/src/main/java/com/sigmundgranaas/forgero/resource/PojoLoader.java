package com.sigmundgranaas.forgero.resource;

import com.sigmundgranaas.forgero.resource.data.v1.ForgeroDataResource;

import java.util.Collection;

public interface PojoLoader<T extends ForgeroDataResource> {
    Collection<T> loadPojos();
}
