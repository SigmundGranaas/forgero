package com.sigmundgranaas.forgero.core.model.v1.match.predicate;

import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public record TypePredicate(Type type) implements Matchable {
	@Override
	public boolean test(Matchable match, MatchContext context) {
		return match.test(type, context) || context.test(type, MatchContext.of());
	}
}
