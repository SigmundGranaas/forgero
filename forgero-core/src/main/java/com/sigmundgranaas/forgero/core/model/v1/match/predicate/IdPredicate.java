package com.sigmundgranaas.forgero.core.model.v1.match.predicate;

import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public record IdPredicate(String id) implements Matchable {
	@Override
	public boolean test(Matchable match, MatchContext context) {
		return match instanceof Identifiable identifiable && (identifiable.identifier().equals(id) || identifiable.identifier().equals("forgero:" + id));
	}
}
