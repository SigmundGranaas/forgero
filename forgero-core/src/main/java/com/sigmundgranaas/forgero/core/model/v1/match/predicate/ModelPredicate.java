package com.sigmundgranaas.forgero.core.model.v1.match.predicate;

import com.sigmundgranaas.forgero.core.model.ModelMatchEntry;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public record ModelPredicate(String model) implements Matchable {
	@Override
	public boolean test(Matchable match, MatchContext context) {
		return match instanceof ModelMatchEntry entry && entry.entry().equals(model);
	}
}
