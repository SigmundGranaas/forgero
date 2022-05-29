package com.sigmundgranaas.forgero.core;

/**
 * The base Interface for all resource objects used by forgero
 * <p>
 * Tools, Tool Parts, materials, Gem, and Schematics
 */
public interface ForgeroResource {
    String getStringIdentifier();

    String getResourceName();

    ForgeroResourceType getResourceType();
}
