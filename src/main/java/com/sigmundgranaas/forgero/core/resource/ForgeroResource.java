package com.sigmundgranaas.forgero.core.resource;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.data.v1.ForgeroDataResource;

/**
 * The base Interface for all resource objects used by forgero
 * <p>
 * Tools, Tool Parts, materials, Gem, and Schematics
 */
public interface ForgeroResource<R extends ForgeroDataResource> {
    String getStringIdentifier();

    default String getNameSpace() {
        return ForgeroInitializer.MOD_NAMESPACE;
    }

    String getResourceName();

    ForgeroResourceType getResourceType();

    R toDataResource();

    default ForgeroResource<R> getResource() {
        return this;
    }
}
