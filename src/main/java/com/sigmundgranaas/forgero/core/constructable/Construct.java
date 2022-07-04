package com.sigmundgranaas.forgero.core.constructable;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;

/**
 * A construct is an element which serves as a foundation for creating a tool part.
 *
 * Construct are the roots of the tool parts, and serves as requirements for creating tool parts.
 * Primary materials and Schematics are the most used Constructs, and serves as the building block for creating basic upgradeable tool parts.
 */
public interface Construct<T> extends PropertyContainer {
    T getResource();

    String getConstructIdentifier();
}
