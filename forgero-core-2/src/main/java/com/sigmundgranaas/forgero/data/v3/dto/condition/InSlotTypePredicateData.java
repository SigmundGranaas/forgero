package com.sigmundgranaas.forgero.data.v3.dto.condition;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier; // Import OpenIdentifier

/**
 * DTO for the "forgero:in_slot_type" predicate.
 * True if the component owning the property is in a slot that accepts the given type tag.
 *
 * @param type     The type of predicate, always "forgero:in_slot_type".
 * @param slotType The type tag of the slot to check for.
 */
public record InSlotTypePredicateData(OpenIdentifier type, OpenIdentifier slotType) implements PredicateData { // Changed type and slotType from String
}
