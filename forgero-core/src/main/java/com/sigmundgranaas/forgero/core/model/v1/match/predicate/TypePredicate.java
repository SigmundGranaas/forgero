package com.sigmundgranaas.forgero.core.model.v1.match.predicate;

import com.sigmundgranaas.forgero.core.state.upgrade.slot.FilledSlot;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;


/**
 * Matches if the type of a FilledSlot equals to the given type, or if the given type is found within a Matchable or a MatchContext.
 */
public record TypePredicate(Type type) implements Matchable {
	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof FilledSlot slot && slot.content().test(type)) {
			return true;
		}
		return match.test(type, context) || context.test(type, MatchContext.of());
	}
}
