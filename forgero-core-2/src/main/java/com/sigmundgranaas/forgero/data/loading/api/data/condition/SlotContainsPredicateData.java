package com.sigmundgranaas.forgero.data.loading.api.data.condition;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * DTO for the "forgero:slot_contains" predicate.
 * True if another slot in the root assembly contains a component with a given tag.
 *
 * @param type    The type of predicate, always "forgero:slot_contains".
 * @param slot    The name of the slot to check (e.g., "handle", "gem").
 * @param tag     The tag that the component in the slot must possess.
 */
public record SlotContainsPredicateData(OpenIdentifier type, String slot, OpenIdentifier tag) implements PredicateData { // Changed type and tag from String
}
