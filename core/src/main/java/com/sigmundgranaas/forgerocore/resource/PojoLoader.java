package com.sigmundgranaas.forgerocore.resource;

import com.sigmundgranaas.forgerocore.data.v1.ForgeroDataResource;

import java.util.Collection;

public interface PojoLoader<T extends ForgeroDataResource> {
    Collection<T> loadPojos();
}
