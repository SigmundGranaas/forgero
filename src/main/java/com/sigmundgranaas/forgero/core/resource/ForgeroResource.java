package com.sigmundgranaas.forgero.core.resource;

import com.sigmundgranaas.forgero.ForgeroInitializer;

/**
 * The base Interface for all resource objects used by forgero
 * <p>
 * Tools, Tool Parts, materials, Gem, and Schematics
 */
public interface ForgeroResource {
    String getStringIdentifier();

    default String getNameSpace() {
        return ForgeroInitializer.MOD_NAMESPACE;
    }

    String getResourceName();

    ForgeroResourceType getResourceType();
}
