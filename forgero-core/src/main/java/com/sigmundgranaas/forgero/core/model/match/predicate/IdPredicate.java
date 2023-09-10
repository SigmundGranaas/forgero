package com.sigmundgranaas.forgero.core.model.match.predicate;

import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.FilledSlot;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * Matches if the given identifier is found within a FilledSlot or an Identifiable.
 */
public record IdPredicate(String id) implements Matchable {


	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof FilledSlot slot && test(slot.content().identifier())) {
			return true;
		}
		return match instanceof Identifiable identifiable && test(identifiable.identifier());
	}

	/**
	 * Matches if the given identifier equals to the id or the id prefixed with "forgero:".
	 */
	private boolean test(String identifier) {
		return (identifier.equals(id) || identifier.equals("forgero:" + id));
	}
}
