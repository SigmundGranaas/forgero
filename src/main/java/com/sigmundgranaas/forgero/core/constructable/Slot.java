package com.sigmundgranaas.forgero.core.constructable;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;

import java.util.Optional;

/**
 * Universal interface representing slots in which you can place elements.
 * Can be empty, or filled with an element giving the parent unique properties.
 *
 * Slots are immutable by default
 */
public interface Slot<T> extends PropertyContainer {
    boolean isFilled();

   default boolean isEmpty(){
       return !isFilled();
   }

    SlotType getType();

    SlotPlacement getPlacement();

    Optional<T> getIfPresent();
}
