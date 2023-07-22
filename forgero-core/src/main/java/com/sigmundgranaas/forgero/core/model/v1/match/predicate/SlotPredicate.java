package com.sigmundgranaas.forgero.core.model.v1.match.predicate;

import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public record SlotPredicate(int index) implements Matchable {
	@Override
	public boolean test(Matchable match, MatchContext context) {
		return match instanceof Slot slot && slot.index() == index && slot.filled();
	}
}
