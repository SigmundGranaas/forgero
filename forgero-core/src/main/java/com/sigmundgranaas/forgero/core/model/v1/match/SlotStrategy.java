package com.sigmundgranaas.forgero.core.model.v1.match;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.upgrade.slot.FilledSlot;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class SlotStrategy implements Matchable {
	private final List<Matchable> predicates;

	public SlotStrategy(List<Matchable> predicates) {
		this.predicates = predicates;
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof FilledSlot slot) {
			var matchesAllSlots = predicates.stream().allMatch(pred -> pred.test(slot, context));
			if (matchesAllSlots) {
				return true;
			}
			var slotContentOpt = slot.get();
			if (slotContentOpt.isPresent()) {
				return predicates.stream().allMatch(pred -> pred.test(slot, context));
			}
		}
		return false;
	}
}
