package com.sigmundgranaas.forgero.core.model.v1.match.predicate;

import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.FilledSlot;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public record IdPredicate(String id) implements Matchable {
	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof FilledSlot slot && test(slot.content().identifier())) {
			return true;
		}
		return match instanceof Identifiable identifiable && test(identifiable.identifier());
	}

	private boolean test(String identifier) {
		return (identifier.equals(id) || identifier.equals("forgero:" + id));
	}
}
