package com.sigmundgranaas.forgero.resource;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.resource.data.v1.ForgeroDataResource;

/**
 * The base Interface for all resource objects used by forgero
 * <p>
 * Tools, Tool Parts, materials, Gem, and Schematics
 */
public interface ForgeroResource<R extends ForgeroDataResource> {
    String getStringIdentifier();

    default String getNameSpace() {
        return Forgero.NAMESPACE;
    }

    String getResourceName();

    ForgeroResourceType getResourceType();

    R toDataResource();

    default ForgeroResource<R> getResource() {
        return this;
    }
}
