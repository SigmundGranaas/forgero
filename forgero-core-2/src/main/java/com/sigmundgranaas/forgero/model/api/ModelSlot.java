package com.sigmundgranaas.forgero.model.api;


import java.util.Optional;

/**
 * Represents a slot within a CompositeModel where another component can be attached.
 *
 * @param id       The identifier for the slot, which should match a component part's ID.
 * @param order    The base rendering order for the component placed in this slot.
 * @param context  An optional context string provided to the component in this slot.
 */
public record ModelSlot(String id, int order, Optional<String> context) {

}
