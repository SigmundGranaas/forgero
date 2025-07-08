package com.sigmundgranaas.forgero.data.dto.condition;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;

/**
 * DTO for the "forgero:in_slot_type" predicate.
 * True if the component owning the property is in a slot that accepts the given type tag.
 *
 * @param type     The type of predicate, always "forgero:in_slot_type".
 * @param slotType The type tag of the slot to check for.
 */
public record InSlotTypePredicateData(OpenIdentifier type, OpenIdentifier slotType) implements PredicateData {
}
