package com.sigmundgranaas.forgero.model.api;

import com.sigmundgranaas.forgero.model.match.Predicate;

import java.util.List;
import java.util.Optional;

/**
 * Represents a conditional variant for a model layer or slot.
 * It can override the texture, the entire model, and/or the offset.
 *
 * @param predicate The list of conditions that must be met for this variant to be active.
 * @param texture   The alternative texture identifier.
 * @param model     The alternative model identifier.
 * @param offset    The alternative offset.
 */
public record ModelVariant(List<Predicate> predicate, Optional<String> texture, Optional<Object> model, Optional<Offset> offset) {
}
