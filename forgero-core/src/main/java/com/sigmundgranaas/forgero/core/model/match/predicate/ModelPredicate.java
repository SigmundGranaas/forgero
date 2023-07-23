package com.sigmundgranaas.forgero.core.model.match.predicate;

import com.sigmundgranaas.forgero.core.model.ModelMatchEntry;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * Matches if the model of a ModelMatchEntry equals to the given model. Models are usually png template files like: "pickaxe_head.png"
 */
public record ModelPredicate(String model) implements Matchable {
	@Override
	public boolean test(Matchable match, MatchContext context) {
		return match instanceof ModelMatchEntry entry && entry.entry().equals(model);
	}
}
