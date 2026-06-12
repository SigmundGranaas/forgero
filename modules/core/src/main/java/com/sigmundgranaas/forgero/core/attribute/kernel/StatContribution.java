package com.sigmundgranaas.forgero.core.attribute.kernel;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;

import java.util.Optional;

/**
 * The atom of the stat kernel: one typed, conditional stat delta.
 *
 * <p>The kernel is a generic tool — it knows no stat names, no content patterns, and no
 * authored scope vocabulary. All semantics are carried by this record's fields:
 *
 * <ul>
 *   <li>{@code operation} — a closed two-element algebra. ADD sums into the base;
 *       MULTIPLY scales the summed base. Sequence is mathematics, never configuration.</li>
 *   <li>{@code gated} — an offered contribution: it only counts in a fold where something
 *       accepts it (a MULTIPLY of the same type from a different source). This is the
 *       generic offer/accept handshake: donors (e.g. materials) offer stats; receivers
 *       (e.g. part templates) accept the ones valid for them by multiplying. Unaccepted
 *       offers are inert — by design, not by silent accident.</li>
 *   <li>{@code local} — self-display only; contributes to the declaring node's own compiled
 *       stats but is never exported upward.</li>
 *   <li>{@code condition} — static (structural) conditions gate inclusion at compile time;
 *       dynamic conditions are never evaluated here: contributions carrying them are lifted
 *       to the terminal as data for the game layer (see DynamicAttributes).</li>
 * </ul>
 */
public record StatContribution(
		OpenIdentifier type,
		Operation operation,
		float value,
		boolean gated,
		boolean local,
		boolean upgradeOnly,
		Optional<Condition> condition
) {
	public enum Operation {ADD, MULTIPLY}

	public static StatContribution add(OpenIdentifier type, float value) {
		return new StatContribution(type, Operation.ADD, value, false, false, false, Optional.empty());
	}

	public static StatContribution multiply(OpenIdentifier type, float factor) {
		return new StatContribution(type, Operation.MULTIPLY, factor, false, false, false, Optional.empty());
	}
}
